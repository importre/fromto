package io.github.importre.fromto

import rx.Observable
import rx.Subscription
import java.util.concurrent.atomic.AtomicBoolean

/**
 * An [FtAction] is wrapper of `rx.Observable` and have `running` state.
 *
 * @author [Jaewe Heo](http://import.re)
 *
 * @param T represents the action&#39;s result data
 */
public class FtAction<T> private constructor() {

    private lateinit var fromObservable: Observable<T>

    private var dataView: ((T) -> Unit)? = null
    private var errorView: ((Throwable) -> Unit)? = null
    private var doneView: (() -> Unit)? = null

    private var fromSubscription: Subscription? = null

    private var running: AtomicBoolean = AtomicBoolean(false)
        private set

    /**
     * @return true if it is running, otherwise false
     */
    public fun isRunning() = running.get()

    internal fun subscribe(fromTo: FromTo) {
        fromSubscription?.unsubscribe()
        fromTo.run { if (!isLoading()) view?.showLoading(true) }
        running.set(true)

        fromSubscription = fromObservable.subscribe({
            dataView?.invoke(it)
        }, {
            errorView?.invoke(it)
            finished(fromTo)
        }, {
            doneView?.invoke()
            finished(fromTo)
        })
    }

    private fun finished(fromTo: FromTo) {
        running.set(false)
        fromTo.run { if (!isLoading()) view?.showLoading(false) }
    }

    internal fun unsubscribe(fromTo: FromTo) {
        fromSubscription?.unsubscribe()
        finished(fromTo)
    }

    /**
     *  [FtAction]&#39;s builder.
     */
    public class Builder<T>() {

        private var dataView: ((T) -> Unit)? = null
        private var errorView: ((Throwable) -> Unit)? = null
        private var doneView: (() -> Unit)? = null
        private var fromObservable: Observable<T>? = null

        /**
         * Sets an observable that will be subscribed by [FromTo].
         * It is must be called.
         *
         * @param fromObservable
         * @return [FtAction.Builder]
         */
        public fun from(fromObservable: Observable<T>): Builder<T> {
            this.fromObservable = fromObservable
            return this
        }

        /**
         * Sets view of `to`.
         *
         * @param view
         * @return [FtAction.Builder]
         */
        public fun to(view: (T) -> Unit): Builder<T> {
            this.dataView = view
            return this
        }

        /**
         * Sets view of `to`.
         *
         * @param view
         * @return [FtAction.Builder]
         */
        public fun to(view: Result<T>): Builder<T> {
            this.dataView = { view.call(it) }
            return this
        }

        /**
         * Sets view of `error`.
         *
         * @param view
         * @return [FtAction.Builder]
         */
        public fun error(view: (Throwable) -> Unit): Builder<T> {
            this.errorView = view
            return this
        }

        /**
         * Sets view of `error`.
         *
         * @param view
         * @return [FtAction.Builder]
         */
        public fun error(view: Error): Builder<T> {
            this.errorView = { view.call(it) }
            return this
        }

        /**
         * Sets view of `done`.
         *
         * @param view
         * @return [FtAction.Builder]
         */
        public fun done(view: () -> Unit): Builder<T> {
            this.doneView = view
            return this
        }

        /**
         * Sets view of `done`.
         *
         * @param view
         * @return [FtAction.Builder]
         */
        public fun done(view: Done): Builder<T> {
            this.doneView = { view.call() }
            return this
        }

        /**
         * Constructs an [FtAction] with the current attributes.
         *
         * @return [FtAction]
         */
        public fun build(): FtAction<T> {
            val action = FtAction<T>()

            if (fromObservable == null) {
                throw NullPointerException("[FROMTO] `from` observable is null")
            } else {
                action.fromObservable = fromObservable as Observable<T>
            }

            action.dataView = dataView
            action.errorView = errorView
            action.doneView = doneView
            return action
        }
    }

    /**
     * Event listener interface for [FtAction]&#39;s result.
     */
    interface Result<T> {
        /**
         * Invoked if [FtAction.fromObservable]&#39;s `onNext` is called.
         *
         * @param result
         */
        fun call(result: T)
    }

    /**
     * Event listener interface for [FtAction]&#39;s error.
     */
    interface Error {
        /**
         * Invoked if [FtAction.fromObservable]&#39;s `onError` is called.
         *
         * @param error
         */
        fun call(error: Throwable)
    }

    /**
     * Event listener interface for [FtAction]&#39;s done.
     */
    interface Done {
        /**
         * Invoked if [FtAction.fromObservable]&#39;s `onCompleted` is called.
         */
        fun call()
    }
}