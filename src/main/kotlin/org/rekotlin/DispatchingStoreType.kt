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
 * Defines the interface of a dispatching, stateless Store in ReKotlin. `StoreType` is
 * the default usage of this interface. Can be used for store variables where you don't
 * care about the state, but want to be able to dispatch actions.
 */
interface DispatchingStoreType {

    /**
     * Dispatches an action. This is the simplest way to modify the stores state.
     *
     * Example of dispatching an action:
     * <pre>
     * <code>
     * store.dispatch( CounterAction.IncreaseCounter )
     * </code>
     * </pre>
     *
     * @param action The action that is being dispatched to the store
     * @return By default returns the dispatched action, but middlewares can change the type, e.g. to return promises
     */
    fun dispatch(action: Action)
}