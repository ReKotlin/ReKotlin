package org.rekotlin

/**
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

/**
 * Defines the interface of Stores in ReKotlin. `Store` is the default implementation of this
 * interface. Applications have a single store that stores the entire application state.
 * Stores receive actions and use reducers combined with these actions, to calculate state changes.
 * Upon every state update a store informs all of its subscribers.
 */
interface StoreType<State : StateType> : DispatchingStoreType {

    /**
     * The current state stored in the store.
     */
    val state: State

    /**
     * The main dispatch function that is used by all convenience `dispatch` methods.
     * This dispatch function can be extended by providing middlewares.
     */
    var dispatchFunction: DispatchFunction

    /**
     * Subscribes the provided subscriber to this store.
     * Subscribers will receive a call to `newState` whenever the
     * state in this store changes.
     * @param subscriber: Subscriber that will receive store updates
     */
    fun <S : StoreSubscriber<in State>> subscribe(subscriber: S)

    /**
     * Subscribes the provided subscriber to this store.
     * Subscribers will receive a call to `newState` whenever the
     * state in this store changes and the subscription decides to forward
     * state update.
     *
     * @param subscriber Subscriber that will receive store updates
     * @param transform A closure that receives a simple subscription and can return a
     * transformed subscription. Subscriptions can be transformed to only select a subset of the
     * state, or to skip certain state updates.
     */
    fun <SelectedState, S : StoreSubscriber<in SelectedState>> subscribe(
        subscriber: S,
        transform: ((Subscription<State>) -> Subscription<SelectedState>)?
    )

    /**
     * Unsubscribes the provided subscriber. The subscriber will no longer
     * receive state updates from this store.
     *
     * @param subscriber Subscriber that will be unsubscribed
     */
    fun <SelectedState> unsubscribe(subscriber: StoreSubscriber<SelectedState>)

    /**
     * Dispatches an action creator to the store. Action creators are functions that generate
     * actions. They are called by the store and receive the current state of the application
     * and a reference to the store as their input.
     *
     * Based on that input the action creator can either return an action or not. Alternatively
     * the action creator can also perform an asynchronous operation and dispatch a new action
     * at the end of it.
     *
     * Example of an action creator:
     * <pre>
     * <code>
     * func deleteNote(noteID: Int) -> ActionCreator {
     *     return { state, store in
     *         // only delete note if editing is enabled
     *         if (state.editingEnabled == true) {
     *             return NoteDataAction.DeleteNote(noteID)
     *         } else {
     *             return nil
     *         }
     *      }
     * }
     * </code>
     * </pre>
     *
     * This action creator can then be dispatched as following:
     * <pre>
     * <code>
     * store.dispatch( noteActionCreatore.deleteNote(3) )
     * </code>
     * </pre>
     *
     * @return: By default returns the dispatched action, but middlewares can change the
     * return type, e.g. to return promises
     */
    fun dispatch(actionCreator: ActionCreator<State, StoreType<State>>)

    /**
     * Dispatches an async action creator to the store. An async action creator generates an
     * action creator asynchronously.
     */
    fun dispatch(asyncActionCreator: AsyncActionCreator<State, StoreType<State>>)

    /**
     * Dispatches an async action creator to the store. An async action creator generates an
     * action creator asynchronously. Use this method if you want to wait for the state change
     * triggered by the asynchronously generated action creator.
     *
     * This overloaded version of `dispatch` calls the provided `callback` as soon as the
     * asynchronoously dispatched action has caused a new state calculation.
     *
     * If the ActionCreator does not dispatch an action, the callback block will never
     * be called
     */
    fun dispatch(asyncActionCreator: AsyncActionCreator<State, StoreType<State>>, callback: DispatchCallback<State>?)
}