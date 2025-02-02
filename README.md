# Kombucha UDF

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.ikarenkov/kombucha-core?style=flat&label=kombucha-core&link=https%3A%2F%2Fcentral.sonatype.com%2Fartifact%2Fio.github.ikarenkov%2Fkombucha-core)](https://central.sonatype.com/artifact/io.github.ikarenkov/kombucha-core)

Kombucha UDF is a UDF library based on The Elm Architecture (TEA) concepts that helps to focus on logic of you application, rather then understanding
what is going on.

# Core concept

## Store

Store maintains the application's state and orchestrates the interactions between components.
Basicly, Store is a base UDF components to interact, that holds state and can accept some messages to handle. It has one input and two outputs:

<img src="https://github.com/ikarenkov/Kombucha-UDF/assets/17216532/09fff09b-f7f9-42c9-ab73-ce3f2f25fb9a" width="700">

Lets take a look to Store main components:

<img src="https://github.com/ikarenkov/Kombucha-UDF/assets/17216532/9669c03c-2d9d-4610-a30a-58619a4bcfb7">

* Reducer - *pure function*, generates a new state and produces effects that require handling. 
* EffectHandler - receives effects from the reducer and has the ability to send messages back to trigger the reducer. Our way to comunicate with the outer world, including any operations that are not pure. F.e. requesting data from db or api or saving data, requesting random and e.t.c.

## Models

There are 3 main model: Msg, State, Eff.
Msg - describes intention to do something. It can be user click, result from your back end or anything else.
State - the state that is stored in store. It represents the state of your feature.
Eff - describes side effects, that should be executed. It's just a description of your intention to to something that is not a pure function. F.e. it
can be request to load some data from backend, saving or reading data from database and e.t.c.

