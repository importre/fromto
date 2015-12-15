package io.github.importre.fromto

import org.junit.Test
import rx.Observable
import rx.schedulers.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FromToTest {

    val view = object : FtView {
        override fun showLoading(show: Boolean) {
        }
    }

    @Test
    fun ShouldBeInitialized() {
        val action = FtAction.Builder<Int>()
                .from(Observable.just(1))
                .to {}
                .error {}
                .build()
        val fromTo1 = FromTo.create(action)
        assertEquals(1, fromTo1.actions.size)

        val actions = listOf(
                FtAction.Builder<Int>()
                        .from(Observable.just(1))
                        .to {}
                        .error {}
                        .build(),
                FtAction.Builder<Int>()
                        .from(Observable.just(1))
                        .to {}
                        .error {}
                        .build())
        val fromTo2 = FromTo.create(actions)
        assertEquals(actions.size, fromTo2.actions.size)
    }

    @Test
    fun ShouldBeSuccessful() {
        val count = CountDownLatch(2)

        val action1 = FtAction.Builder<Int>()
                .from(Observable.just(1).subscribeOn(Schedulers.newThread()))
                .to(object : FtAction.Result<Int> {
                    override fun call(result: Int) {
                        assertEquals(1, result); count.countDown()
                    }
                })
                .error {}
                .build()

        val action2 = FtAction.Builder<Int>()
                .from(Observable.just(1).subscribeOn(Schedulers.newThread()))
                .to { assertEquals(1, it); count.countDown() }
                .error {}
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
    fun ShouldBeSuccessfulWithDone() {
        val count = CountDownLatch(2)

        val action = FtAction.Builder<Int>()
                .from(Observable.just(1).subscribeOn(Schedulers.newThread()))
                .to { assertEquals(1, it); count.countDown() }
                .done { count.countDown() }
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
                .to {}
                .error { count.countDown() }
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
