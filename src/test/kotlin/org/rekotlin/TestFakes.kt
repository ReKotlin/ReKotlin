package org.rekotlin

/**
 * Created by Taras Vozniuk on 10/08/2017.
 * Copyright © 2017 GeoThings. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

internal data class TestAppState(val testValue: Int? = null) : StateType

internal data class TestStringAppState(val testValue: String = "Initial") : StateType

internal data class TestCustomSubstate(val value: Int) : StateType

internal data class TestBlockState(val testAppState: TestAppState? = null, val testStringAppState: TestStringAppState? = null) : StateType

internal data class TestCustomAppState(val substate: TestCustomSubstate) : StateType {
    constructor(substateValue: Int = 0) : this(TestCustomSubstate(substateValue))
}

internal data class NoOpAction(val unit: Unit = Unit) : Action
internal data class SetValueAction(val value: Int?) : Action {
    companion object {
        val type = "SetValueAction"
    }
}

internal data class SetValueStringAction(var value: String) : Action {
    companion object {
        val type = "SetValueStringAction"
    }
}

internal data class SetCustomSubstateAction(val value: Int) : Action {
    companion object {
        val type = "SetCustomSubstateAction"
    }
}

internal class TestReducer {
    fun handleAction(action: Action, state: TestAppState?): TestAppState {
        @Suppress("NAME_SHADOWING")
        var state = state ?: TestAppState()

        when (action) {
            is SetValueAction -> {
                state = state.copy(testValue = action.value)
            }
        }

        return state
    }
}

internal class TestValueStringReducer {
    fun handleAction(action: Action, state: TestStringAppState?): TestStringAppState {
        @Suppress("NAME_SHADOWING")
        var state = state ?: TestStringAppState()

        when (action) {
            is SetValueStringAction -> {
                state = state.copy(testValue = action.value)
            }
        }

        return state
    }
}

internal class TestCustomAppStateReducer {
    fun handleAction(action: Action, state: TestCustomAppState?): TestCustomAppState {
        @Suppress("NAME_SHADOWING")
        var state = state ?: TestCustomAppState()

        when (action) {
            is SetCustomSubstateAction -> {
                state = state.copy(substate = state.substate.copy(action.value))
            }
        }

        return state
    }
}

internal fun blockStateReducer(action: Action, testBlockState: TestBlockState?): TestBlockState =
        TestBlockState(
                testAppState = TestReducer().handleAction(action, testBlockState?.testAppState),
                testStringAppState = TestValueStringReducer().handleAction(action, testBlockState?.testStringAppState)
        )

internal class TestStoreSubscriber<T> : StoreSubscriber<T> {
    var recievedStates: MutableList<T> = mutableListOf()

    override fun newState(state: T) {
        this.recievedStates.add(state)
    }
}

internal class DispatchingSubscriber(var store: Store<TestAppState>) : StoreSubscriber<TestAppState> {

    override fun newState(state: TestAppState) {
        // Test if we've already dispatched this action to
        // avoid endless recursion
        if (state.testValue != 5) {
            this.store.dispatch(SetValueAction(5))
        }
    }
}

internal class CallbackStoreSubscriber<T>(val handler: (T) -> Unit) : StoreSubscriber<T> {
    override fun newState(state: T) {
        handler(state)
    }
}

