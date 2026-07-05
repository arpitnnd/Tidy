package com.example.urlcleanapp

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class Main(val sharedUrl: String? = null) : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object History : NavKey
