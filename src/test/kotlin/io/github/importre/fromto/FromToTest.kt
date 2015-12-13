package io.github.importre.fromto

import org.junit.Test
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FromToTest {

    val toSubject = PublishSubject.create<Int>()
    val finishSubject = PublishSubject.create<Unit>()
    val errorSubject = PublishSubject.create<Throwable>()

    val view = object : FtView {
        override fun showLoading(show: Boolean) {
        }
    }

    @Test
    fun ShouldBeInitialized() {
        val action = FtAction.Builder<Int>()
                .from(Observable.just(1))
                .to(toSubject, {})
                .error(errorSubject, {})
                .build()
        val fromTo = FromTo.create(action)
        assertEquals(1, fromTo.actions.size)
    }

    @Test
    fun ShouldBeSuccessful() {
        val count = CountDownLatch(2)

        val action1 = FtAction.Builder<Int>()
                .from(Observable.just(1).subscribeOn(Schedulers.newThread()))
                .to(toSubject, { assertEquals(1, it); count.countDown() })
                .error(errorSubject, {})
                .build()

        val action2 = FtAction.Builder<Int>()
                .from(Observable.just(1).subscribeOn(Schedulers.newThread()))
                .to(toSubject, { assertEquals(1, it); count.countDown() })
                .error(errorSubject, {})
                .build()

        val fromTo = FromTo.create(action1, action2)
        fromTo.attach(view).execute()
        assertTrue(fromTo.isLoading())

        count.await(1000, TimeUnit.MILLISECONDS)
        assertEquals(0, count.count)

        fromTo.detach()
        assertFalse(fromTo.isLoading())
    }

    @Test
    fun ShouldBeSuccessfulWithFinish() {
        val count = CountDownLatch(2)

        val action = FtAction.Builder<Int>()
                .from(Observable.just(1).subscribeOn(Schedulers.newThread()))
                .to(toSubject, { assertEquals(1, it); count.countDown() })
                .finish(finishSubject, { count.countDown() })
                .build()

        val fromTo = FromTo.create(action)
        fromTo.attach(view).execute()
        assertTrue(fromTo.isLoading())

        count.await(1000, TimeUnit.MILLISECONDS)
        assertEquals(0, count.count)

        fromTo.detach()
        assertFalse(fromTo.isLoading())
    }

    @Test
    fun ShouldBeFailed() {
        val count = CountDownLatch(1)

        val action = FtAction.Builder<Int>()
                .from(Observable.create<Int> {
                    val i: Int? = null
                    it.onNext(i!!)
                }.subscribeOn(Schedulers.newThread()))
                .to(toSubject, {})
                .error(errorSubject, { count.countDown() })
                .build()

        val fromTo = FromTo.create(action)
        fromTo.attach(view).execute()
        assertTrue(fromTo.isLoading())

        count.await(1000, TimeUnit.MILLISECONDS)
        assertEquals(0, count.count)

        fromTo.detach()
        assertFalse(fromTo.isLoading())
    }
}
