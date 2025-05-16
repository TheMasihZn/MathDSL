@file:Suppress("unused")

package com.github.themasihzn.mathdsl

@Target(AnnotationTarget.FUNCTION)
annotation class Math(val symbol: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Super

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Sub
