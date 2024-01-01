package ru.ikarenkov.teamaker.sample.counter.impl

import ru.ikarenkov.teamaker.eff_handler.adaptCast
import ru.ikarenkov.teamaker.sample.counter.api.counterFeatureFacade
import ru.ikarenkov.teamaker.store.Store
import ru.ikarenkov.teamaker.store.StoreFactory
import ru.ikarenkov.teamaker.store.SyncStore

internal class CounterStoreFactory(
    private val counterEffectHandler: CounterEffectHandler,
) {

    fun create(initialState: State): Store<Msg, State, Eff> = SyncStore(
        initialState,
        rootReducer::invoke,
        listOf(counterEffectHandler.adaptCast()),
    )

}

internal class CounterCoroutineStoreFactory(
    private val storeFactory: StoreFactory,
    private val counterEffectHandler: CounterFlowEffectHandler,
) {

    fun create(initialState: State): Store<Msg, State, Eff> = storeFactory.create(
        "Counter",
        initialState,
        rootReducer::invoke,
        effectHandlers = arrayOf(counterEffectHandler.adaptCast()),
    )

}

internal fun createCounterStore(initialState: State): Store<Msg, State, Eff> =
    counterFeatureFacade.scope.get<CounterStoreFactory>().create(initialState)