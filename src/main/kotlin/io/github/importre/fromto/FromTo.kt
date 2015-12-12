package io.github.importre.fromto

public class FromTo private constructor(val actions: Array<out FtAction<*>>) {

    companion object {
        @JvmStatic
        fun create(vararg actions: FtAction<*>): FromTo = FromTo(actions)
    }

    var view: FtView? = null
        private set

    public fun attach(view: FtView): FromTo {
        this.view = view
        return this
    }

    public fun execute() {
        if (!isLoading()) {
            actions.forEach { it.subscribe(this) }
        }
    }

    public fun detach() {
        actions.forEach { it.unsubscribe(this) }
        this.view = null
    }

    public fun isLoading(): Boolean {
        actions.forEach { if (it.isRunning()) return true }
        return false
    }
}