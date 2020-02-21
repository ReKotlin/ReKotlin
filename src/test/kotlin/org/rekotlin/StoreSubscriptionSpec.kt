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

package org.rekotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StoreSubscriptionTests {

    // this is not going to work in JVM.
    // WeakReference also can't solve it since gc collects non-deterministically
    // TODO: Discuss with ReSwift community for this inconsistency
    /*
    /**
     * It does not strongly capture an observer
     */
    @Test
    fun testStrongCapture(){
        store = Store(reducer::handleAction, TestAppState())
        var subscriber: TestSubscriber? = TestSubscriber()

        store.subscribe(subscriber!!)
        assertEquals(1, store.subscriptions.map { it.subscriber != null }.count())

        @Suppress("UNUSED_VALUE")
        subscriber = null
        assertEquals(0, store.subscriptions.map { it.subscriber != null }.count())
    }
    */

    /**
     * it removes subscribers before notifying state changes
     */
    @Test
    fun testRemoveSubscribers() {
        val store = Store(::testReducer, TestAppState())
        val subscriber1 = TestStoreSubscriber<TestAppState>()
        val subscriber2 = TestStoreSubscriber<TestAppState>()

        store.subscribe(subscriber1)
        store.subscribe(subscriber2)
        store.dispatch(SetValueAction(3))
        assertEquals(2, store.subscriptions.count())
        assertEquals(3, subscriber1.receivedStates.lastOrNull()?.testValue)
        assertEquals(3, subscriber2.receivedStates.lastOrNull()?.testValue)

        // dereferencing won't remove the subscriber(like in ReSwift)
        // subscriber1 = null
        store.unsubscribe(subscriber1)
        store.dispatch(SetValueAction(5))
        assertEquals(1, store.subscriptions.count())
        assertEquals(5, subscriber2.receivedStates.lastOrNull()?.testValue)

        // dereferencing won't remove the subscriber(like in ReSwift)
        // subscriber1 = null
        store.unsubscribe(subscriber2)
        store.dispatch(SetValueAction(8))
        assertEquals(0, store.subscriptions.count())
    }

    /**
     * it replaces the subscription of an existing subscriber with the new one.
     */
    @Test
    fun testDuplicateSubscription() {
        val store = Store(::testReducer, TestAppState())
        val subscriber = TestStoreSubscriber<TestAppState>()

        // initial subscription
        store.subscribe(subscriber)
        // Subsequent subscription that skips repeated updates.
        store.subscribe(subscriber) { skipRepeats { oldState, newState -> oldState.testValue == newState.testValue } }

        // One initial state update for every subscription.
        assertEquals(2, subscriber.receivedStates.count())

        store.dispatch(SetValueAction(3))
        store.dispatch(SetValueAction(3))
        store.dispatch(SetValueAction(3))
        store.dispatch(SetValueAction(3))

        assertEquals(3, subscriber.receivedStates.count())
    }

    /**
     * it dispatches initial value upon subscription
     */
    @Test
    fun testDispatchInitialValue() {
        val store = Store(::testReducer, TestAppState())
        val subscriber = TestStoreSubscriber<TestAppState>()

        store.subscribe(subscriber)
        store.dispatch(SetValueAction(3))

        assertEquals(3, subscriber.receivedStates.lastOrNull()?.testValue)
    }

    /**
     * it allows dispatching from within an observer
     */
    @Test
    fun testAllowDispatchWithinObserver() {
        val store = Store(::testReducer, TestAppState())
        val subscriber = DispatchingSubscriber(store)

        store.subscribe(subscriber)
        store.dispatch(SetValueAction(2))

        assertEquals(5, store.state.testValue)
    }

    /**
     * it does not dispatch value after subscriber unsubscribes
     */
    @Test
    fun testDontDispatchToUnsubscribers() {
        val store = Store(::testReducer, TestAppState())
        val subscriber = TestStoreSubscriber<TestAppState>()

        store.dispatch(SetValueAction(5))
        store.subscribe(subscriber)
        store.dispatch(SetValueAction(10))

        store.unsubscribe(subscriber)
        // Following value is missed due to not being subscribed:
        store.dispatch(SetValueAction(15))
        store.dispatch(SetValueAction(25))

        store.subscribe(subscriber)
        store.dispatch(SetValueAction(20))

        assertEquals(4, subscriber.receivedStates.count())
        assertEquals(5, subscriber.receivedStates[subscriber.receivedStates.count() - 4].testValue)
        assertEquals(10, subscriber.receivedStates[subscriber.receivedStates.count() - 3].testValue)
        assertEquals(25, subscriber.receivedStates[subscriber.receivedStates.count() - 2].testValue)
        assertEquals(20, subscriber.receivedStates[subscriber.receivedStates.count() - 1].testValue)
    }

    /**
     * it ignores identical subscribers
     */
    @Test
    fun testIgnoreIdenticalSubscribers() {
        val store = Store(::testReducer, TestAppState())
        val subscriber = TestStoreSubscriber<TestAppState>()

        store.subscribe(subscriber)
        store.subscribe(subscriber)

        assertEquals(1, store.subscriptions.count())
    }

    /**
     * it ignores identical subscribers that provide substate selectors
     */
    @Test
    fun testIgnoreIdenticalSubstateSubscribers() {
        val store = Store(::testReducer, TestAppState())
        val subscriber = TestStoreSubscriber<TestAppState>()

        store.subscribe(subscriber) { this }
        store.subscribe(subscriber) { this }

        assertEquals(1, store.subscriptions.count())
    }

    @Test
    fun testSubscribeDuringOnNewState() {
        // setup
        val store = Store(reducer = ::stringStateReducer, state = TestStringAppState())

        val subscribeA = ViewSubscriberTypeA(store)
        store.subscribe(subscribeA)

        // execute
        store.dispatch(SetValueStringAction("subscribe"))
    }

    @Test
    fun testUnSubscribeDuringOnNewState() {
        // setup
        val store = Store(reducer = ::stringStateReducer, state = TestStringAppState())

        val subscriberA = ViewSubscriberTypeA(store)
        val subscriberC = ViewSubscriberTypeC()
        store.subscribe(subscriberA)
        store.subscribe(subscriberC)

        // execute
        store.dispatch(SetValueStringAction("unsubscribe"))
    }
}