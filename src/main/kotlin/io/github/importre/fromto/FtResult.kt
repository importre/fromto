package io.github.importre.fromto

/**
 * This class represents the result of [FtView]&#39;s callback.
 *
 * If [complete] is true, [result] will be null.
 * Otherwise, [result] is null or not null. It depends on you observable.
 *
 * @see [FtAction.Builder.from]
 */
class FtResult<T>(val result: T?, val complete: Boolean)
