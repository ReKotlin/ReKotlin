package org.rekotlin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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

internal val firstMiddleware: Middleware<StateType> = { dispatch, getState ->
    { next ->
        { action ->
            (action as? SetValueStringAction)?.let {
                it.value += " First Middleware"
                next(action)
            } ?: next(action)
        }
    }
}

internal val secondMiddleware: Middleware<StateType> = { dispatch, getState ->
    { next ->
        { action ->
            (action as? SetValueStringAction)?.let {
                it.value += " Second Middleware"
                next(action)
            } ?: next(action)
        }
    }
}

internal val dispatchingMiddleware: Middleware<StateType> = { dispatch, getState ->
    { next ->
        { action ->
            (action as? SetValueAction)?.let {
                dispatch(SetValueStringAction("${it.value ?: 0}"))
            }

            next(action)
        }
    }
}

internal val stateAccessingMiddleware: Middleware<TestStringAppState> = { dispatch, getState ->
    { next ->
        { action ->

            val appState = getState()
            val stringAction = action as? SetValueStringAction

            // avoid endless recursion by checking if we've dispatched exactly this action
            if (appState?.testValue == "OK" && stringAction?.value != "Not OK") {
                // dispatch a new action
                dispatch(SetValueStringAction("Not OK"))

                // and swallow the current one
                dispatch(NoOpAction())
            } else {
                next(action)
            }
        }
    }
}

internal class StoreMiddlewareTests {

    /**
     * it can decorate dispatch function
     */
    @Test
    fun testDecorateDispatch() {

        val reducer = TestValueStringReducer()
        val store = Store(
                reducer = reducer::handleAction,
                state = TestStringAppState(),
                middleware = listOf(firstMiddleware, secondMiddleware)
        )

        val subscriber = TestStoreSubscriber<TestStringAppState>()
        store.subscribe(subscriber)

        val action = SetValueStringAction("OK")
        store.dispatch(action)

        assertEquals("OK First Middleware Second Middleware", store.state.testValue)
    }

    /**
     * it can dispatch actions
     */
    @Test
    fun testCanDispatch() {

        val reducer = TestValueStringReducer()
        val store = Store(
                reducer = reducer::handleAction,
                state = TestStringAppState(),
                middleware = listOf(firstMiddleware, secondMiddleware, dispatchingMiddleware)
        )

        val subscriber = TestStoreSubscriber<TestStringAppState>()
        store.subscribe(subscriber)

        val action = SetValueAction(10)
        store.dispatch(action)

        assertEquals("10 First Middleware Second Middleware", store.state.testValue)
    }

    /**
     * it middleware can access the store's state
     */
    @Test
    fun testMiddlewareCanAccessState() {

        val reducer = TestValueStringReducer()
        var state = TestStringAppState()
        state = state.copy(testValue = "OK")

        val store = Store(
                reducer = reducer::handleAction,
                state = state,
                middleware = listOf(stateAccessingMiddleware)
        )

        store.dispatch(SetValueStringAction("Action That Won't Go Through"))
        assertEquals("Not OK", store.state.testValue)
    }
}

