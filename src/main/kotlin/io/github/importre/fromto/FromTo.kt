package io.github.importre.fromto

/**
 * A [FromTo] is an unit of [FtAction]s based on [RxJava](https://goo.gl/8Sf6OZ).
 *
 * See [readme.md](https://github.com/importre/fromto/blob/master/readme.md).
 *
 * @author [Jaewe Heo](http://import.re)
 *
 * @property actions [FtAction] list.
 * @property view interface of a view related with [FromTo].
 */
public class FromTo private constructor(val actions: List<FtAction<*>>) {

    /**
     * @see [FromTo]
     */
    companion object {
        /**
         * Creates a [FromTo] with [FtAction]s.
         *
         * @param actions [FtAction]s as [Varargs](https://goo.gl/y13f9G)
         */
        @JvmStatic
        fun create(vararg actions: FtAction<*>): FromTo {
            return FromTo(actions.toList())
        }

        /**
         * Creates a [FromTo] with [FtAction]s.
         *
         * @param actions [FtAction]s as [List]
         */
        @JvmStatic
        fun create(actions: List<FtAction<*>>): FromTo = FromTo(actions)
    }

    internal var view: FtView? = null
        private set

    /**
     * Attaches [view] to a view related with [FromTo]
     */
    public fun attach(view: FtView): FromTo {
        this.view = view
        return this
    }

    /**
     * Executes(Subscribes) [actions].
     */
    public fun execute() {
        if (!isLoading()) {
            actions.forEach { it.subscribe(this) }
        }
    }

    /**
     * Detaches [view] and `unsubscribes` [actions].
     */
    public fun detach() {
        actions.forEach { it.unsubscribe(this) }
        this.view = null
    }

    /**
     * Checks whether [actions] are loading or not.
     *
     * @return true if one of [actions] is loading, otherwise false
     */
    public fun isLoading(): Boolean {
        actions.forEach { if (it.isRunning()) return true }
        return false
    }
}