package com.github.themasihzn.mathdsl.folding

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MathSymbol(val symbol: String)