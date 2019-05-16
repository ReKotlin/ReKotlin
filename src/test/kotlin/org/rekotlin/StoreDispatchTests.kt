package org.rekotlin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

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

internal typealias TestSubscriber = TestStoreSubscriber<TestAppState>

internal typealias CallbackSubscriber = CallbackStoreSubscriber<TestAppState>

internal class StoreDispatchTests {

    var reducer = TestReducer()
    var store = Store(reducer::handleAction, TestAppState())

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    /**
     * it throws an exception when a reducer dispatches an action
     */
    @Test
    fun testThrowsExceptionWhenReducersDispatch() {
        //TODO: testThrowsExceptionWhenReducersDispatch
    }

    /**
     * it accepts action creators
     */
    @Test
    fun testAcceptsActionCreators() {
        store.dispatch(SetValueAction(5))

        val doubleValueActionCreator: ActionCreator<TestAppState, StoreType<TestAppState>> = { state, store ->
            SetValueAction(state.testValue!! * 2)
        }

        store.dispatch(doubleValueActionCreator)
        assertEquals(10, store.state.testValue)
    }

    /**
     * it accepts async action creators
     */
    @Test
    fun testAcceptsAsyncActionCreators() {

        val awaitEntity = CountDownLatch(1)

        val asyncActionCreator: AsyncActionCreator<TestAppState, StoreType<TestAppState>> = { _, _, callback ->
            thread {
                // Provide the callback with an action creator
                callback { _, _ ->
                    SetValueAction(5)
                }
            }
        }

        val subscriber = CallbackStoreSubscriber<TestAppState> { state ->
            this.store.state.testValue?.let {
                assertEquals(5, it)
                awaitEntity.countDown()
            }
        }

        store.subscribe(subscriber)
        store.dispatch(asyncActionCreator)
        assertTrue(awaitEntity.await(1, TimeUnit.SECONDS))
    }

    /**
     * it calls the callback once state update from async action is complete
     */
    @Test
    fun testCallsCallbackOnce() {

        val awaitEntity = CountDownLatch(1)

        val asyncActionCreator: AsyncActionCreator<TestAppState, StoreType<TestAppState>> = { _, _, callback ->
            thread {
                // Provide the callback with an action creator
                callback { _, _ ->
                    SetValueAction(5)
                }
            }
        }

        store.dispatch(asyncActionCreator) { newState ->
            assertEquals(5, this.store.state.testValue)
            if (newState.testValue == 5) {
                awaitEntity.countDown()
            }
        }

        assertTrue(awaitEntity.await(1, TimeUnit.SECONDS))
    }
}

