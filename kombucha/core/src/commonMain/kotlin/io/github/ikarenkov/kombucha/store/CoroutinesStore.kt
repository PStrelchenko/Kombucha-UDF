package io.github.ikarenkov.kombucha.store

import io.github.ikarenkov.kombucha.DefaultStoreCoroutineExceptionHandler
import io.github.ikarenkov.kombucha.eff_handler.EffectHandler
import io.github.ikarenkov.kombucha.reducer.Reducer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Basic coroutines safe implementation of [Store]. State modification is consequential with locking using [Mutex].
 * @param name - the name that is used in base [EffectHandler] and in [coroutinesScope] as [CoroutineName], to help you in debugging.
 * @param reducer - the main logic component that describes how new state and side effects are provides. Must be a pure function.
 * @param effectHandlers - the list of objects that are intended to handle all effects that [reducer] provides.
 * @param initialState - the initial state that is stored in our store.
 * @param initialEffects - the set of effect which are going to be send to [effectHandlers] after creation of this objects.
 * Can be used to initialise some subscriptions or to make some set up actions.
 * @param coroutineExceptionHandler - the handler to handle all unhandled exceptions from [reducer] and [effectHandlers].
 */
open class CoroutinesStore<Msg : Any, Model : Any, Eff : Any>(
    name: String?,
    private val reducer: Reducer<Msg, Model, Eff>,
    private val effectHandlers: List<EffectHandler<Eff, Msg>> = listOf(),
    initialState: Model,
    initialEffects: Set<Eff> = setOf(),
    coroutineExceptionHandler: CoroutineExceptionHandler = DefaultStoreCoroutineExceptionHandler()
) : Store<Msg, Model, Eff> {

    private val mutableState = MutableStateFlow(initialState)
    override val state: StateFlow<Model> = mutableState

    private val mutableEffects = MutableSharedFlow<Eff>()
    override val effects: Flow<Eff> = mutableEffects

    private val isCanceled: Boolean
        get() = !coroutinesScope.isActive

    open val coroutinesScope = StoreScope(name, coroutineExceptionHandler)

    private val stateUpdateMutex = Mutex()

    private val mutableStoreUpdates: MutableSharedFlow<StoreUpdate<Msg, Model, Eff>> = MutableSharedFlow()
    val storeUpdates: SharedFlow<StoreUpdate<Msg, Model, Eff>> = mutableStoreUpdates

    init {
        initEffHandlers(initialEffects)
    }

    override fun accept(msg: Msg) {
        coroutinesScope.launch {
            val storeUpdate = stateUpdateMutex.withLock {
                if (!isCanceled) {
                    val oldState = state.value
                    val (newState, effects) = reducer(msg, oldState)
                    mutableState.value = newState
                    StoreUpdate(
                        msg = msg,
                        oldState = oldState,
                        newState = newState,
                        effects = effects
                    )
                } else {
                    null
                }
            }
            if (storeUpdate != null) {
                mutableStoreUpdates.emit(storeUpdate)
                storeUpdate.effects.forEach { eff ->
                    launch {
                        mutableEffects.emit(eff)
                        handleEff(eff)
                    }
                }
            }
        }
    }

    override fun cancel() {
        coroutinesScope.cancel()
    }

    private fun initEffHandlers(initialEffects: Set<Eff>) {
        initialEffects.forEach {
            handleEff(it)
        }
    }

    private fun handleEff(eff: Eff) {
        effectHandlers.forEach { effHandler ->
            coroutinesScope.launch {
                effHandler
                    .handleEff(eff = eff)
                    .collect { accept(it) }
            }
        }
    }

}

data class StoreUpdate<Msg, State, Eff>(
    val msg: Msg,
    val oldState: State,
    val newState: State,
    val effects: Set<Eff>
)
