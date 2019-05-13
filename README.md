## ReKotlin

[![License MIT](https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](https://github.com/ReSwift/ReSwift/blob/master/LICENSE.md)
[![Build Status](https://travis-ci.org/ReKotlin/ReKotlin.svg?branch=master)](https://travis-ci.org/ReKotlin/ReKotlin)
[![Download](https://api.bintray.com/packages/rekotlin/rekotlin/rekotlin/images/download.svg) ](https://bintray.com/rekotlin/rekotlin/rekotlin/_latestVersion)

Port of [ReSwift](https://github.com/ReSwift/ReSwift) to Kotlin, which corresponds to [ReSwift/4.0.0](https://github.com/ReSwift/ReSwift/releases/tag/4.0.0)

## Introduction

ReKotlin is a [Redux](https://github.com/reactjs/redux)-like implementation of the unidirectional data flow architecture in Kotlin. ReKotlin helps you to separate three important concerns of your app's components:

- **State**: in a ReKotlin app the entire app state is explicitly stored in a data structure. This helps avoid complicated state management code, enables better debugging and has many, many more benefits...
- **Views**: in a ReKotlin app your views update when your state changes. Your views become simple visualizations of the current app state.
- **State Changes**: in a ReKotlin app you can only perform state changes through actions. Actions are small pieces of data that describe a state change. By drastically limiting the way state can be mutated, your app becomes easier to understand and it gets easier to work with many collaborators.

The ReKotlin library is tiny - allowing users to dive into the code, understand every single line and hopefully contribute.

## About ReKotlin

ReKotlin relies on a few principles:
- **The Store** stores your entire app state in the form of a single data structure. This state can only be modified by dispatching Actions to the store. Whenever the state in the store changes, the store will notify all observers.
- **Actions** are a declarative way of describing a state change. Actions don't contain any code, they are consumed by the store and forwarded to reducers. Reducers will handle the actions by implementing a different state change for each action.
- **Reducers** provide pure functions, that based on the current action and the current app state, create a new app state

![](Docs/img/reswift_concept.png)

For a very simple app, that maintains a counter that can be increased and decreased, you can define the app state as following:

```kotlin
data class AppState (
        val counter: Int = 0
): StateType
```

You would also define two actions, one for increasing and one for decreasing the counter. For the simple actions in this example we can define empty data classes that conform to action:

```kotlin
data class CounterActionIncrease(val unit: Unit = Unit): Action
data class CounterActionDecrease(val unit: Unit = Unit): Action
```

Your reducer needs to respond to these different action types, that can be done by switching over the type of action:

```kotlin
fun counterReducer(action: Action, state: AppState?): AppState {
    // if no state has been provided, create the default state
    var state = state ?: AppState()

    when(action){
        is CounterActionIncrease -> {
            state = state.copy(counter = state.counter + 1)
        }
        is CounterActionDecrease -> {
            state = state.copy(counter = state.counter - 1)
        }
    }

    return state
}
```
In order to have a predictable app state, it is important that the reducer is always free of side effects, it receives the current app state and an action and returns the new app state.

To maintain our state and delegate the actions to the reducers, we need a store. Let's call it `mainStore` and define it as a global constant, for example in the Main Activity file:

```kotlin
val mainStore = Store(
     reducer = ::counterReducer,
     state = null
)

class MainActivity : AppCompatActivity(){
	//...
}
```


Lastly, your view layer, in this case an activity,
needs to tie into this system by subscribing to store updates and
emitting actions whenever the app state needs to be changed
(assuming that `snake_case` View properties are coming from [Kotlin Android Extensions](https://kotlinlang.org/docs/tutorials/android-plugin.html)):

```kotlin
class MainActivity : AppCompatActivity(), StoreSubscriber<AppState> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // when either button is tapped, an action is dispatched to the store
        // in order to update the application state
        button_up.setOnClickListener {
            mainStore.dispatch(CounterActionIncrease())
        }
        button_down.setOnClickListener {
            mainStore.dispatch(CounterActionDecrease())
        }

        // subscribe to state changes
        mainStore.subscribe(this)
    }

    override fun newState(state: AppState) {
        // when the state changes, the UI is updated to reflect the current state
        counter_label.text = "${state.counter}"
    }
}
```

The `newState` method will be called by the `Store` whenever a new app state is available, this is where we need to adjust our view to reflect the latest app state.

When working with multiple states in a single class, BlockSubscriber can be used for listening to states in it's specific closure instead of using StoreSubscriber<>

```kotlin
class MainActivity : AppCompatActivity() {

    private val counterLabel: TextView by lazy {
        this.findViewById(R.id.counter_label) as TextView
    }

    private val buttonUp: Button by lazy {
        this.findViewById(R.id.button) as Button
    }

    private val buttonDown: Button by lazy {
        this.findViewById(R.id.button2) as Button
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val appStateSubscriber = BlockSubscriber<AppState> { appState ->
            this.counterLabel.text = "${appState.counter}"
        }

        // when either button is tapped, an action is dispatched to the store
        // in order to update the application state
        this.buttonUp.setOnClickListener {
            mainStore.dispatch(CounterActionIncrease())
        }
        this.buttonDown.setOnClickListener {
            mainStore.dispatch(CounterActionDecrease())
        }

        // subscribe to state changes
        mainStore.subscribe(appStateSubscriber)
    }
}
```

Button taps result in dispatched actions that will be handled by the store and its reducers, resulting in a new app state.

This is a very basic example that only shows a subset of ReKotlin's features, read the Getting Started Guide __(not ported yet)__ to see how you can build entire apps with this architecture.

[You can also watch this talk on the motivation behind the original ReSwift](https://realm.io/news/benji-encz-unidirectional-data-flow-swift/).

## Examples

- [ReduxMovieExample](https://github.com/ReKotlin/ReduxMovieExample) - An application which uses an API to display movies and stores favorites into a local database.
- [rekotlin-router-github-example](https://github.com/ReKotlin/rekotlin-router-github-example) - An application which displays user's github repositories along with authentication and navigation using [rekotlin-router](https://github.com/ReKotlin/rekotlin-router)
- [ReKotlin-CounterExample](https://github.com/GeoThings/ReKotlin-CounterExample) - A simple counter application

## Why ReKotlin?

Model-View-Controller (MVC) is not a holistic application architecture. Typical apps defer a lot of complexity to controllers since MVC doesn't offer other solutions for state management, one of the most complex issues in app development.

Apps built upon MVC often end up with a lot of complexity around state management and propagation. We need to use callbacks, delegations, Key-Value-Observation and notifications to pass information around in our apps and to ensure that all the relevant views have the latest state.

This approach involves a lot of manual steps and is thus error prone and doesn't scale well in complex code bases.

It also leads to code that is difficult to understand at a glance, since dependencies can be hidden deep inside of view controllers. Lastly, you mostly end up with inconsistent code, where each developer uses the state propagation procedure they personally prefer. You can circumvent this issue by style guides and code reviews but you cannot automatically verify the adherence to these guidelines.

ReKotlin attempts to solve these problem by placing strong constraints on the way applications can be written. This reduces the room for programmer error and leads to applications that can be easily understood - by inspecting the application state data structure, the actions and the reducers.

This architecture provides further benefits beyond improving your code base:

- Stores, Reducers, Actions and extensions such as [ReSwift Router](https://github.com/ReKotlin/rekotlin-router)  are entirely platform independent - you can easily use the same business logic and share it between apps for multiple platforms
- Want to collaborate with a co-worker on fixing an app crash? Use __(port not yet available)__ [ReSwift Recorder](https://github.com/ReSwift/ReSwift-Recorder) to record the actions that lead up to the crash and send them the JSON file so that they can replay the actions and reproduce the issue right away.
- Maybe recorded actions can be used to build UI and integration tests?

The ReKotlin tooling is still in a very early stage, but aforementioned prospects excite us and hopefully others in the community as well!

## Getting Started Guide

Getting started guide has not yet been ported. In the meantime, please refer to original ReSwift's:
[Getting Started Guide that describes the core components of apps built with ReSwift](http://reswift.github.io/ReSwift/master/getting-started-guide.html). 

To get an understanding of the core principles we recommend reading the brilliant [redux documentation](http://redux.js.org/).

## Installation

```gradle
dependencies {
    implementation 'org.rekotlin:rekotlin:1.0.0'
}
```

## Differences with ReSwift

### Dereferencing subscribers will not result in subscription removed

In ReSwift when you dereference the subscriber or it goes out of the scope, you won't receive new state updates. 

```swift
var subscriber: TestSubscriber? = TestSubscriber()
store.subscribe(subscriber!)
subscriber = nil
```

However in ReKotlin you need make sure you have unsubscribed explicitly.

```kotlin
val subscriber = TestSubscriber()
store.subscribe(subscriber)
store.unsubscribe(subscriber)
```

### Equatability and skipRepeats

When subscribing without substate selection like `store.subscribe(someSubscriber)` in swift you need to have your state implementing Equatable in order to skipRepeats being applied automatically.

```swift
public struct State: StateType {
    public let mapState: MapState
    public let appState: AppState
}

extension State: Equatable {
    public static func ==(lhs: State, rhs: State) -> Bool {
        //...
    }
}

```

However in Kotlin(JVM) every object implements `equals()`, so that skipRepeats will be applied automatically when you `store.subscribe(someSubscriber)`, with Kotlin [Structural Equality](https://kotlinlang.org/docs/reference/equality.html#structural-equality) check used.

Please note, if you implement your states/substates with [data classes](https://kotlinlang.org/docs/reference/data-classes.html), Kotlin compiler will automatically derive non-shallow `equals()` from all properties declared in the primary constructor. 

If you want to opt-out of this behaviour please set `automaticallySkipRepeats` to __false__ in your store declaration:

```kotlin
val store = Store(
	reducer::handleAction, 
	state, 
	automaticallySkipRepeats = false)
```

### Subscribe/Unsubscribe during `newState`

Under the hood ReKotlin uses a [`CopyOnWriteArrayList`](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CopyOnWriteArrayList.html) to manage subscriptions (see [PR 29](https://github.com/ReKotlin/ReKotlin/pull/29) for more details). This implementation detail means that the number of concurrent writes to the subscriptions should be less than the number of concurrent reads.

In terms of using the library this means that un/subscribing may incur a performance overhead if done during `newState` in store subscribers. We recommend to restrict this usage (concurrent write while reading subscriptions) as much as possible, i.e. avoid `subscribe` or `unsubscribe` in calls to `newState`.

## Contributing

Please format your code using ``kotlinFormatter.xml`` file from [here](Docs/kotlinFormatter.xml)
Using this code formatter will help us maintain consistency in code style.

## Credits

- Many thanks to [Benjamin Encz](https://github.com/Ben-G) and other ReSwift contributors for buidling original [ReSwift](https://github.com/ReSwift/ReSwift) that we really enjoyed working with.
- Also huge thanks to [Dan Abramov](https://github.com/gaearon) for building [Redux](https://github.com/reactjs/redux) - all ideas in here and many implementation details were provided by his library.

