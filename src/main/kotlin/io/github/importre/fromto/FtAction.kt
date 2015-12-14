package io.github.importre.fromto

import rx.Observable
import rx.Subscription
import rx.subjects.Subject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A [FtAction] executes [rx.Observable] and notifies [rx.Subject] of the result.
 *
 * @author [Jaewe Heo](http://import.re)
 *
 * @param T represents the action&#39;s result data
 */
public class FtAction<T> private constructor() {

    private lateinit var fromObservable: Observable<T>

    private lateinit var toSubject: Subject<T, T>
    private lateinit var dataView: ((T) -> Unit)

    private var finishSubject: Subject<Unit, Unit>? = null
    private var finishView: (() -> Unit)? = null

    private var errorSubject: Subject<Throwable, Throwable>? = null
    private var errorView: ((Throwable) -> Unit)? = null

    private var fromSubscription: Subscription? = null
    private var toSubscription: Subscription? = null
    private var finishSubscription: Subscription? = null
    private var errorSubscription: Subscription? = null

    private var running: AtomicBoolean = AtomicBoolean(false)
        private set

    /**
     * @return true if it is running, otherwise false
     */
    public fun isRunning() = running.get()

    private fun init() {
        toSubscription?.unsubscribe()
        toSubscription = toSubject.subscribe({
            dataView.invoke(it)
        }, {
            errorSubject?.onNext(it)
        })

        finishSubscription?.unsubscribe()
        finishSubscription = finishSubject?.subscribe({
            finishView?.invoke()
        }, {
        })

        errorSubscription?.unsubscribe()
        errorSubscription = errorSubject?.subscribe({
            errorView?.invoke(it)
        }, {
            errorView?.invoke(it)
        })
    }

    fun subscribe(fromTo: FromTo) {
        fromSubscription?.unsubscribe()
        fromTo.run { if (!isLoading()) view?.showLoading(true) }
        running.set(true)

        fromSubscription = fromObservable.subscribe({
            toSubject.onNext(it)
        }, {
            errorSubject?.onError(it)
            finished(fromTo)
        }, {
            finishSubject?.onNext(Unit)
            finished(fromTo)
        })
    }

    private fun finished(fromTo: FromTo) {
        running.set(false)
        fromTo.run { if (!isLoading()) view?.showLoading(false) }
    }

    fun unsubscribe(fromTo: FromTo) {
        fromSubscription?.unsubscribe()
        toSubscription?.unsubscribe()
        finished(fromTo)
    }

    /**
     *  [FtAction]&#39;s builder.
     */
    public class Builder<T>() {

        private var dataView: ((T) -> Unit)? = null
        private var finishView: (() -> Unit)? = null
        private var errorView: ((Throwable) -> Unit)? = null
        private var fromObservable: Observable<T>? = null
        private var toSubject: Subject<T, T>? = null
        private var finishSubject: Subject<Unit, Unit>? = null
        private var errorSubject: Subject<Throwable, Throwable>? = null

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
         * Sets subject and view of `to`. It is must be called.
         * [view] will be invoked with the result via [subject].`onNext()`
         *
         * @param view
         * @param subject
         * @return [FtAction.Builder]
         */
        public fun to(view: ((T) -> Unit),
                      subject: Subject<T, T>): Builder<T> {
            this.toSubject = subject
            this.dataView = view
            return this
        }

        /**
         * Sets subject and view of `finish`.
         * [view] will be invoked with the result via [subject].`onNext()`
         *
         * @param view
         * @param subject
         * @return [FtAction.Builder]
         */
        public fun finish(view: (() -> Unit),
                          subject: Subject<Unit, Unit>): Builder<T> {
            this.finishSubject = subject
            this.finishView = view
            return this
        }

        /**
         * Sets subject and view of `error`.
         * [view] will be invoked with the result via [subject].`onNext()`
         *
         * @param view
         * @param subject
         * @return [FtAction.Builder]
         */
        public fun error(view: ((Throwable) -> Unit),
                         subject: Subject<Throwable, Throwable>): Builder<T> {
            this.errorSubject = subject
            this.errorView = view
            return this
        }

        /**
         * Constructs a [FtAction] with the current attributes.
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

            if (dataView == null) {
                throw NullPointerException("[FROMTO] `dataView` is null")
            } else {
                action.dataView = dataView as (T) -> Unit
            }

            if (toSubject == null) {
                throw NullPointerException("[FROMTO] `to` subject is null")
            } else {
                action.toSubject = toSubject as Subject<T, T>
            }

            action.finishView = finishView
            action.finishSubject = finishSubject as? Subject<Unit, Unit>
            action.errorView = errorView
            action.errorSubject = errorSubject as? Subject<Throwable, Throwable>
            action.init()
            return action
        }
    }
}