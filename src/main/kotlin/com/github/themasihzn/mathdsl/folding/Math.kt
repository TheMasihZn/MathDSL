@file:Suppress("unused")

package com.github.themasihzn.mathdsl.folding

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Math(val symbol: String)