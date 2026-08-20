package com.example.inscit.utils

import com.example.inscit.models.Lang
import java.util.Random

object DailyFacts {
    private const val MS_PER_DAY = 24L * 60 * 60 * 1000

    val englishFacts: List<String> = listOf(
        "Honey never spoils. Archaeologists have found 3,000-year-old honey in Egyptian tombs that was still edible.",
        "Your brain generates about 20 watts of electrical power — enough to power a dim light bulb.",
        "Octopuses have three hearts and blue blood.",
        "A single bolt of lightning is about 5 times hotter than the surface of the Sun.",
        "The human body contains roughly 37 trillion cells.",
        "Bananas are berries, but strawberries are not.",
        "Light takes about 8 minutes and 20 seconds to travel from the Sun to Earth.",
        "There are more stars in the universe than grains of sand on all of Earth's beaches.",
        "The average human body is made of about 60% water.",
        "A day on Venus is longer than a year on Venus.",
        "Your stomach has to produce a new layer of mucus every two weeks, or it would digest itself.",
        "The strongest muscle in the human body relative to its size is the masseter (jaw muscle).",
        "Trees can communicate and share nutrients through underground fungal networks called mycorrhizae.",
        "Neutron stars are so dense that a sugar-cube-sized amount would weigh about a billion tons.",
        "The DNA in a single human cell, if unrolled, would be about 2 meters long.",
        "Sharks existed before trees. Sharks have been around for about 400 million years.",
        "The human nose can detect over one trillion different smells.",
        "Jupiter's Great Red Spot is a storm larger than Earth that has raged for at least 350 years.",
        "An iceberg's mass is about 90% below the water surface — only about 10% is visible.",
        "Your body replaces about 3.8 million cells every second.",
        "The Moon is slowly drifting away from Earth at about 3.8 centimeters per year.",
        "Sound travels about 4.3 times faster in water than in air.",
        "Humans share about 60% of their DNA with bananas.",
        "The Tyrannosaurus rex lived closer in time to humans than to the Stegosaurus.",
        "A teaspoon of a neutron star would have the mass of about 900 Great Pyramids of Giza.",
        "The Earth's core is about as hot as the surface of the Sun.",
        "Sloths can hold their breath longer than dolphins — up to 40 minutes.",
        "Your heart beats about 100,000 times every single day.",
        "Ants can carry up to 50 times their own body weight.",
        "The observable universe is about 93 billion light-years in diameter."
    )

    val hindiFacts: List<String> = listOf(
        "शहद कभी खराब नहीं होता। पुरातत्वविदों ने मिस्र के कब्रों में 3,000 साल पुराना शहद पाया जो अभी भी खाने योग्य था।",
        "आपका मस्तिष्क लगभग 20 वाट विद्युत ऊर्जा पैदा करता है — इतनी कि एक बल्ब जलाया जा सके।",
        "ऑक्टोपस के तीन दिल होते हैं और रक्त नीले रंग का होता है।",
        "बिजली की एक कड़क सूर्य की सतह से लगभग 5 गुना अधिक गर्म होती है।",
        "मानव शरीर में लगभग 37 खरब कोशिकाएं होती हैं।",
        "केले बेरी हैं, जबकि स्ट्रॉबेरी बेरी नहीं है।",
        "प्रकाश को सूर्य से पृथ्वी तक पहुंचने में लगभग 8 मिनट 20 सेकंड लगते हैं।",
        "ब्रह्मांड में पृथ्वी के सभी समुद्र तटों की रेत के कणों से अधिक तारे हैं।",
        "मानव शरीर का लगभग 60% हिस्सा पानी है।",
        "शुक्र पर एक दिन, शुक्र पर एक वर्ष से भी लंबा होता है।",
        "आपका पेट हर दो सप्ताह में बलगम की नई परत बनाता है, अन्यथा वह खुद को पचा लेता।",
        "आकार की तुलना में मानव शरीर की सबसे मजबूत मांसपेशी जॉ (जबड़े) की मांसपेशी है।",
        "पेड़ माइकोराइजा नामक भूमिगत फंगल नेटवर्क के माध्यम से संवाद और पोषक तत्व साझा कर सकते हैं।",
        "न्यूट्रॉन तारे इतने घने होते हैं कि चीनी के एक टुकड़े जितनी मात्रा का वजन लगभग एक अरब टन होगा।",
        "एक कोशिका का DNA खोलने पर लगभग 2 मीटर लंबा होता है।",
        "शार्क पेड़ों से पहले अस्तित्व में थीं। शार्क लगभग 400 मिलियन वर्षों से हैं।",
        "मानव नाक एक ट्रिलियन से अधिक अलग गंध पहचान सकती है।",
        "बृहस्पति का ग्रेट रेड स्पॉट पृथ्वी से बड़ा तूफान है जो कम से कम 350 वर्षों से चल रहा है।",
        "हिमखंड का लगभग 90% द्रव्यमान पानी के नीचे होता है — केवल 10% दिखाई देता है।",
        "आपका शरीर हर सेकंड लगभग 38 लाख कोशिकाओं को बदलता है।",
        "चंद्रमा पृथ्वी से प्रति वर्ष लगभग 3.8 सेंटीमीटर दूर खिसक रहा है।",
        "पानी में ध्वनि हवा की तुलना में लगभग 4.3 गुना तेज चलती है।",
        "मनुष्य केले के साथ अपना लगभग 60% DNA साझा करते हैं।",
        "टी-रेक्स, स्टेगोसॉरस की तुलना में मनुष्यों के समय के अधिक करीब रहता था।",
        "न्यूट्रॉन तारे का एक चम्मच, गीज़ा के लगभग 900 महा पिरामिडों के बराबर द्रव्यमान रखता है।",
        "पृथ्वी का केंद्र सूर्य की सतह जितना गर्म है।",
        "स्लॉथ डॉल्फ़िन से अधिक समय तक सांस रोक सकते हैं — 40 मिनट तक।",
        "आपका हृदय हर दिन लगभग 100,000 बार धड़कता है।",
        "चींटियां अपने शरीर के वजन का 50 गुना तक उठा सकती हैं।",
        "दृश्यमान ब्रह्मांड लगभग 93 अरब प्रकाश-वर्ष चौड़ा है।"
    )

    fun getTodaysFact(lang: Lang): String {
        val list = if (lang == Lang.HI) hindiFacts else englishFacts
        val dayIndex = System.currentTimeMillis() / MS_PER_DAY
        val random = Random(dayIndex)
        return list[random.nextInt(list.size)]
    }
}