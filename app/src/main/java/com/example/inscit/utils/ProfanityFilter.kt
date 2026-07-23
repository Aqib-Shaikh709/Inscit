package com.example.inscit.utils

import java.util.Locale

object ProfanityFilter {
    // A simplified map of bad words to mild alternatives.
    // In a real app, this would be a much larger database or an API call.
    private val profanityMap = mapOf(
        // English
        "fuck" to "heck",
        "shit" to "stuff",
        "ass" to "back",
        "bitch" to "mean person",
        "bastard" to "rascal",
        "hell" to "heck",
        "damn" to "darn",
        "stupid" to "unwise",
        "idiot" to "friend",
        "hate" to "dislike",
        
        // Hindi (Devanagari)
        "गाली" to "शब्द", // Placeholder
        "बकवास" to "व्यर्थ",
        "बेवकूफ" to "नादान",
        "साला" to "मित्र",
        "कमीना" to "शरारती",
        
        // Hinglish
        "chutiya" to "bhai",
        "gaali" to "baat",
        "bakwas" to "vyarth",
        "bevakoof" to "nadan",
        "saala" to "dost",
        "kamina" to "shararati",
        "bc" to "bhai",
        "mc" to "bhai"
    )

    /**
     * Checks if the text contains any bad words.
     */
    fun containsBadWords(text: String): Boolean {
        val lowerText = text.lowercase(Locale.ROOT)
        return profanityMap.keys.any { badWord ->
            Regex("(?i)(?<![\\p{L}])${Regex.escape(badWord)}(?![\\p{L}])").containsMatchIn(lowerText)
        }
    }

    /**
     * Converts bad words to mild words.
     */
    fun convertToMild(text: String): String {
        var processedText = text
        profanityMap.forEach { (bad, mild) ->
            val regex = Regex("(?i)(?<![\\p{L}])${Regex.escape(bad)}(?![\\p{L}])") // Unicode-aware, whole word
            processedText = processedText.replace(regex, mild)
        }
        return processedText
    }

    /**
     * Masks bad words with asterisks.
     */
    fun maskBadWords(text: String): String {
        var processedText = text
        profanityMap.keys.forEach { bad ->
            val regex = Regex("(?i)(?<![\\p{L}])${Regex.escape(bad)}(?![\\p{L}])")
            processedText = processedText.replace(regex, "*".repeat(bad.length))
        }
        return processedText
    }

    /**
     * Returns a processed version of the text if it contains profanity.
     * Mild replacement for the alert, and masked version for the actual storage/display.
     */
    fun processReview(text: String): Triple<String, String, Boolean> {
        val hasBadWords = containsBadWords(text)
        if (!hasBadWords) return Triple(text, text, false)

        val mild = convertToMild(text)
        val masked = maskBadWords(text)
        
        return Triple(mild, masked, true)
    }
}
