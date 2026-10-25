package com.tidy.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// shareSequence disambiguates repeated navigation events: NavKey's default content key is this
// data class's own toString(), so two Main instances with the same sharedText or two upsell
// requests would otherwise collide and silently inherit the first entry's saved state.
@Serializable
data class Main(
    val sharedText: String? = null,
    val shareSequence: Int = 0,
    val showPlusUpsell: Boolean = false
) : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object History : NavKey

@Serializable
data object About : NavKey
