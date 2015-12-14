package io.github.importre.fromto

import rx.Observable
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.Subject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A [FtAction] executes `rx.Observable` and notifies `rx.Subject` of the result.
 *
 * @author [Jaewe Heo](http://import.re)
 *
 * @param T represents the action&#39;s result data
 */
public class FtAction<T> private constructor() {

    private lateinit var fromObservable: Observable<T>

    private lateinit var toSubject: Subject<FtResult<T>, FtResult<T>>
    private lateinit var dataView: ((FtResult<T>) -> Unit)

    private var errorSubject: Subject<Throwable, Throwable>? = null
    private var errorView: ((Throwable) -> Unit)? = null

    private var fromSubscription: Subscription? = null
    private var toSubscription: Subscription? = null
    private var errorSubscription: Subscription? = null

    private var complete: Boolean = false
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
            toSubject.onNext(FtResult(it, false))
        }, {
            errorSubject?.onError(it)
            finished(fromTo)
        }, {
            if (complete) {
                toSubject.onNext(FtResult(null, true))
            }
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

        private var complete: Boolean = false
        private var dataView: ((FtResult<T>) -> Unit)? = null
        private var errorView: ((Throwable) -> Unit)? = null
        private var fromObservable: Observable<T>? = null
        private var toSubject: Subject<FtResult<T>, FtResult<T>>? = null
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
         * If you want to receive `onComplete`, set [complete] to `true`.
         * Then view will be invoked with `null`
         *
         * @param view
         * @param subject
         * @param complete
         * @return [FtAction.Builder]
         */
        public fun to(view: ((FtResult<T>) -> Unit),
                      subject: Subject<FtResult<T>, FtResult<T>> = BehaviorSubject.create(),
                      complete: Boolean = false)
                : Builder<T> {
            this.toSubject = subject
            this.dataView = view
            this.complete = complete
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
                         subject: Subject<Throwable, Throwable> = BehaviorSubject.create())
                : Builder<T> {
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
                action.dataView = dataView as (FtResult<T>) -> Unit
            }

            if (toSubject == null) {
                throw NullPointerException("[FROMTO] `to` subject is null")
            } else {
                action.toSubject = toSubject as Subject<FtResult<T>, FtResult<T>>
            }

            action.complete = complete
            action.errorView = errorView
            action.errorSubject = errorSubject as? Subject<Throwable, Throwable>
            action.init()
            return action
        }
    }
}