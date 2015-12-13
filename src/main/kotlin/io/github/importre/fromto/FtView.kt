package io.github.importre.fromto

/**
 * Interface definition for a callback to be invoked
 * when one of [FromTo]&apos;s actions has been started or finished.
 *
 * @author [Jaewe Heo](http://import.re)
 */
public interface FtView {

    /**
     * Called when one of [FromTo]&apos;s actions has been started or finished.
     *
     * @param show The loading state of [FromTo]
     */
    fun showLoading(show: Boolean)
}