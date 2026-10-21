package com.tidy.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrackerDescriptionsTest {
    @Test
    fun exactExplanationWinsEvenWhenPatternComesFirst() {
        val descriptions = linkedMapOf("utm_*" to "Campaign", "utm_source" to "Source")

        assertEquals("Source", descriptions.trackerDescriptionFor(" UTM_SOURCE "))
        assertEquals("Campaign", descriptions.trackerDescriptionFor("utm_custom"))
    }

    @Test
    fun explanationPatternsUseTheSameLiteralMatchingAsCleaning() {
        val descriptions = mapOf("track.*_id" to "Tracking identifier")
        val trackers = descriptions.map { TrackerEntry(it.key, it.value) }
        val result = UrlCleaner().clean(
            "https://example.com/?track.partner_id=1&trackXpartner_id=2&track.partner_id_suffix=3",
            trackers = trackers
        )

        assertEquals(listOf("track.partner_id"), result.removedParams)
        assertEquals("Tracking identifier", descriptions.trackerDescriptionFor("track.partner_id"))
        assertNull(descriptions.trackerDescriptionFor("trackXpartner_id"))
        assertNull(descriptions.trackerDescriptionFor("track.partner_id_suffix"))
    }

    @Test
    fun unknownAndBlankParametersHaveNoInventedExplanation() {
        val descriptions = mapOf("utm_*" to "Campaign")

        assertNull(descriptions.trackerDescriptionFor("custom_tracker"))
        assertNull(descriptions.trackerDescriptionFor(" "))
        assertNull(emptyMap<String, String>().trackerDescriptionFor("utm_source"))
    }
}
