package com.example.inscit.syllabus

import com.example.inscit.Branch
import com.example.inscit.models.Lang

data class NoteEntry(
    val title: String,
    val content: String,
    val branch: Branch,
    val lang: Lang
)

object Syllabus {
    // Titles aligned 1:1 with TopicSyllabus getTopics() ids p1-p12/c1-c12/b1-b12.
    // Previously these drifted ("Motion Basics", "Mathematical Waves", "Bohr Model",
    // "Cell Biology"...), so zip() paired content with the wrong title.
    fun getSyllabusNotes(branch: Branch, lang: Lang): List<NoteEntry> {
        val titles = when (branch) {
            Branch.PHYSICS -> if (lang == Lang.EN) listOf(
                "Kinematics", "Newton's Laws", "Work and Energy", "Light Reflection", "Heat Transfer",
                "Wave Mechanics", "Electromagnetism", "Nuclear Physics",
                "Fluid Mechanics", "Thermodynamics", "Optics & Photonics", "Modern Physics"
            ) else listOf(
                "गतिकी", "न्यूटन के नियम", "कार्य और ऊर्जा", "प्रकाश परावर्तन", "ऊष्मा स्थानांतरण",
                "तरंग यांत्रिकी", "विद्युत चुंबकत्व", "परमाणु भौतिकी",
                "तरल यांत्रिकी", "ऊष्मागतिकी", "प्रकाशिकी", "आधुनिक भौतिकी"
            )
            Branch.CHEMISTRY -> if (lang == Lang.EN) listOf(
                "States of Matter", "Elements & Mixtures", "Atomic Structure", "Chemical Reactions", "Acids and Bases",
                "Quantum Theory", "Periodic Trends", "Chemical Bonding",
                "Electrochemistry", "Organic Chemistry", "Thermochemistry", "Environmental Chemistry"
            ) else listOf(
                "पदार्थ की अवस्थाएं", "तत्व और मिश्रण", "परमाणु संरचना", "रासायनिक अभिक्रियाएं", "अम्ल और क्षार",
                "क्वांटम सिद्धांत", "आवर्त प्रवृत्तियाँ", "रासायनिक बंधन",
                "विद्युत रसायन", "कार्बनिक रसायन", "ताप रसायन", "पर्यावरण रसायन"
            )
            Branch.BIOLOGY -> if (lang == Lang.EN) listOf(
                "Cell Structure", "Plant Tissues", "Metabolism", "Respiration", "DNA & Heredity",
                "Endocrine System", "Nervous System", "Ecology",
                "Genetics & Evolution", "Microbiology", "Human Physiology", "Biotechnology"
            ) else listOf(
                "कोशिका संरचना", "पादप ऊतक", "चयापचय", "श्वसन", "DNA और आनुवंशिकता",
                "अंतःस्रावी तंत्र", "तंत्रिका तंत्र", "पारिस्थितिकी",
                "आनुवंशिकी", "सूक्ष्मजीव", "मानव शरीर विज्ञान", "जैव प्रौद्योगिकी"
            )
        }

        val contents = when (branch) {
            Branch.PHYSICS -> PhysicsSyllabus.getExplanations(lang)
            Branch.CHEMISTRY -> ChemistrySyllabus.getExplanations(lang)
            Branch.BIOLOGY -> BiologySyllabus.getExplanations(lang)
        }

        // Guard length drift: zip() silently drops extras, so fall back to paired size
        val size = minOf(titles.size, contents.size)
        return titles.take(size).zip(contents.take(size)).map { (title, content) ->
            NoteEntry(title, content, branch, lang)
        }
    }
}
