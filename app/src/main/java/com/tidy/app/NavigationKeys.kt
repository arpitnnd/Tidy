package com.tidy.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// shareSequence disambiguates repeated shares of the identical URL: NavKey's default content
// key is this data class's own toString(), so two Main instances with the same sharedUrl
// would otherwise collide (same back-stack contentKey), and the second share would silently
// inherit the first entry's saved state -- including hasHandledThisSharedUrl -- and do
// nothing. Irrelevant to entries created any other way, which just take the default 0.
@Serializable
data class Main(val sharedUrl: String? = null, val shareSequence: Int = 0) : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object History : NavKey

@Serializable
data object About : NavKey
