package com.example.inscit.quiz

import com.example.inscit.models.Lang

class QuizEngine {
    fun getQuestions(
        lang: Lang, 
        count: Int = 10, 
        difficulty: String? = null
    ): List<ScienceQuestion> {
        val all = when (lang) {
            Lang.HI -> getHindiQuestions()
            else -> getEnglishQuestions()
        }
        val filtered = if (difficulty != null) {
            val byDifficulty = all.filter { it.difficulty == difficulty }
            if (byDifficulty.isEmpty()) {
                all
            } else {
                byDifficulty
            }
        } else all

        return filtered.shuffled().take(count)
    }

    fun calculateAnalytics(
        questions: List<ScienceQuestion>,
        userAnswers: Map<String, QuizOption>
    ): ScienceAnalytics {
        val totalQuestions = questions.size
        val correctCount = userAnswers.values.count { it.isCorrect }
        val overallScore = ((correctCount.toFloat() / totalQuestions) * 100).toInt()

        val domains = ScienceDomain.entries
        val radarData = domains.map { domain ->
            val domainQuestions = questions.filter { it.domain == domain }
            val domainScore = if (domainQuestions.isEmpty()) 0f else {
                val domainCorrect = domainQuestions.count { q -> userAnswers[q.id]?.isCorrect == true }
                domainCorrect.toFloat() / domainQuestions.size
            }
            DomainScore(domain, domainScore, domainQuestions.size)
        }.filter { it.totalQuestions > 0 }

        val strengthsEn = radarData.filter { it.score >= 0.8f }.map { it.domain.displayNameEn }
        val strengthsHi = radarData.filter { it.score >= 0.8f }.map { it.domain.displayNameHi }
        val weaknessesEn = radarData.filter { it.score < 0.5f }.map { it.domain.displayNameEn }
        val weaknessesHi = radarData.filter { it.score < 0.5f }.map { it.domain.displayNameHi }

        val explanations = questions.map { q ->
            val isCorrect = userAnswers[q.id]?.isCorrect ?: false
            val status = if (isCorrect) "✓" else "✗"
            "$status ${q.text}" to q.explanation
        }

        return ScienceAnalytics(
            overallScore = overallScore,
            scienceTypeEn = determineScienceTypeEn(overallScore),
            scienceTypeHi = determineScienceTypeHi(overallScore),
            radarData = radarData,
            strengthsEn = strengthsEn,
            strengthsHi = strengthsHi,
            weaknessesEn = weaknessesEn,
            weaknessesHi = weaknessesHi,
            explanations = explanations
        )
    }

    private fun determineScienceTypeEn(score: Int) = when {
        score >= 90 -> "CORE SCIENTIST"
        score >= 70 -> "EXPLORER"
        else -> "NOVICE"
    }

    private fun determineScienceTypeHi(score: Int) = when {
        score >= 90 -> "मुख्य वैज्ञानिक"
        score >= 70 -> "अन्वेषक"
        else -> "नौसिखिया"
    }

    private fun getEnglishQuestions() = listOf(
        // Physics - Basic 5
        ScienceQuestion(
            id = "p1", domain = ScienceDomain.PHYSICS,
            text = "Velocity is a scalar quantity?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Velocity is a vector as it includes direction. Speed is the scalar equivalent."
        ),
        ScienceQuestion(
            id = "p2", domain = ScienceDomain.PHYSICS,
            text = "Newton's 1st Law is also called Law of Inertia?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Inertia is the tendency of objects to resist changes in their state of motion."
        ),
        ScienceQuestion(
            id = "p3", domain = ScienceDomain.PHYSICS,
            text = "Energy can be created from nothing?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Conservation of Energy states energy only transforms from one form to another."
        ),
        ScienceQuestion(
            id = "p4", domain = ScienceDomain.PHYSICS,
            text = "In reflection, angle of incidence equals angle of reflection?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "This is the primary Law of Reflection for light."
        ),
        ScienceQuestion(
            id = "p5", domain = ScienceDomain.PHYSICS,
            text = "Convection occurs mainly in solids?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Convection requires fluid movement (liquids/gases). Solids transfer heat via conduction."
        ),
        // Chemistry - Basic 5
        ScienceQuestion(
            id = "c1", domain = ScienceDomain.CHEMISTRY,
            text = "Gases have a fixed volume?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Gases expand to fill whatever container they are in, having no fixed shape or volume."
        ),
        ScienceQuestion(
            id = "c2", domain = ScienceDomain.CHEMISTRY,
            text = "A mixture is chemically bonded?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Mixtures are physical combinations; compounds are chemical combinations."
        ),
        ScienceQuestion(
            id = "c3", domain = ScienceDomain.CHEMISTRY,
            text = "Atoms are made of protons, neutrons, and electrons?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "These are the three primary subatomic particles."
        ),
        ScienceQuestion(
            id = "c4", domain = ScienceDomain.CHEMISTRY,
            text = "Rusting of iron is a physical change?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Rusting forms a new substance (iron oxide), making it a chemical change."
        ),
        ScienceQuestion(
            id = "c5", domain = ScienceDomain.CHEMISTRY,
            text = "Acids turn blue litmus paper red?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Acids are sour and turn litmus red; bases turn it blue."
        ),
        // Biology - Basic 5
        ScienceQuestion(
            id = "b1", domain = ScienceDomain.BIOLOGY,
            text = "Plant cells have a rigid cell wall?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "The cell wall provides structure and protection for plant cells."
        ),
        ScienceQuestion(
            id = "b2", domain = ScienceDomain.BIOLOGY,
            text = "Xylem transports food in plants?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Xylem transports water; Phloem transports food (glucose)."
        ),
        ScienceQuestion(
            id = "b3", domain = ScienceDomain.BIOLOGY,
            text = "Photosynthesis happens in the mitochondria?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Photosynthesis occurs in Chloroplasts; Mitochondria are for respiration."
        ),
        ScienceQuestion(
            id = "b4", domain = ScienceDomain.BIOLOGY,
            text = "The heart is part of the circulatory system?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "The heart pumps blood through the transport network of the body."
        ),
        ScienceQuestion(
            id = "b5", domain = ScienceDomain.BIOLOGY,
            text = "Genes carry hereditary information?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Genes are units of heredity passed from parents to offspring."
        ),
        // Original Questions
        ScienceQuestion(
            id = "q1", domain = ScienceDomain.PHYSICS,
            text = "Waves transfer energy, not matter?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Waves propagate energy through a medium without permanent displacement of matter."
        ),
        ScienceQuestion(
            id = "q2", domain = ScienceDomain.PHYSICS,
            text = "Force is mass times acceleration?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Newton's 2nd Law states F = m × a."
        ),
        ScienceQuestion(
            id = "q3", domain = ScienceDomain.CHEMISTRY,
            text = "Bohr model works for hydrogen-like atoms?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "The Bohr model accurately explains single-electron systems like hydrogen, He+, Li2+, etc."
        ),
        ScienceQuestion(
            id = "q4", domain = ScienceDomain.BIOLOGY,
            text = "Animal cells have large vacuoles?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Plant cells have large central vacuoles. Animal cells have smaller, temporary ones."
        ),
        ScienceQuestion(
            id = "q5", domain = ScienceDomain.CHEMISTRY,
            text = "Isotopes have the same neutron count?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Isotopes have identical proton numbers but different neutron counts."
        ),
        ScienceQuestion(
            id = "q6", domain = ScienceDomain.PHYSICS,
            text = "Light always travels at constant speed?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Light speed varies by medium. It's fastest in vacuum (c = 3×10⁸ m/s)."
        ),
        ScienceQuestion(
            id = "q7", domain = ScienceDomain.BIOLOGY,
            text = "DNA replication is semi-conservative?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Each new DNA molecule contains one original strand and one newly synthesized strand."
        ),
        ScienceQuestion(
            id = "q8", domain = ScienceDomain.PHYSICS,
            text = "Kinetic energy depends on velocity squared?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "KE = ½mv². Doubling velocity quadruples kinetic energy."
        ),
        ScienceQuestion(
            id = "q9", domain = ScienceDomain.CHEMISTRY,
            text = "Electrons orbit nucleus like planets?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Electrons exist in probability clouds (orbitals), not fixed orbits."
        ),
        ScienceQuestion(
            id = "q10", domain = ScienceDomain.BIOLOGY,
            text = "Mitochondria produce ATP only?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Mitochondria also regulate calcium, synthesize heme, and signaling."
        ),
        // INTERMEDIATE QUESTIONS - Physics
        ScienceQuestion(
            id = "ip1", domain = ScienceDomain.PHYSICS,
            text = "Escape velocity from Earth is independent of the mass of the object?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "v = sqrt(2GM/R). It only depends on the planet's mass and radius.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ip2", domain = ScienceDomain.PHYSICS,
            text = "Entropy of an isolated system can decrease over time?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Second Law of Thermodynamics states entropy always increases or remains constant.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ip3", domain = ScienceDomain.PHYSICS,
            text = "A p-type semiconductor has holes as majority carriers?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "P-type (Positive) semiconductors are doped to create an excess of holes.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ip4", domain = ScienceDomain.PHYSICS,
            text = "Lenz's Law is a consequence of the law of conservation of energy?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "The induced current opposes the change to conserve energy.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ip5", domain = ScienceDomain.PHYSICS,
            text = "The speed of sound is faster in water than in air?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Sound travels faster in denser, less compressible media.",
            difficulty = "INTERMEDIATE"
        ),
        // INTERMEDIATE QUESTIONS - Chemistry
        ScienceQuestion(
            id = "ic1", domain = ScienceDomain.CHEMISTRY,
            text = "Noble gases have the highest electronegativity in their period?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Noble gases generally have zero electronegativity as they have full shells.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ic2", domain = ScienceDomain.CHEMISTRY,
            text = "A buffer solution resists change in pH when small amounts of acid/base are added?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Buffers contain both weak acid/base and its conjugate to neutralize additions.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ic3", domain = ScienceDomain.CHEMISTRY,
            text = "The hybridization of Carbon in Ethene (C2H4) is sp3?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Carbon in Ethene is sp2 hybridized due to the double bond.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ic4", domain = ScienceDomain.CHEMISTRY,
            text = "Endothermic reactions have a negative Change in Enthalpy (ΔH)?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Endothermic reactions absorb heat, so ΔH is positive.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ic5", domain = ScienceDomain.CHEMISTRY,
            text = "Activation energy is the minimum energy required to start a reaction?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "It is the energy barrier that must be overcome for reactants to become products.",
            difficulty = "INTERMEDIATE"
        ),
        // INTERMEDIATE QUESTIONS - Biology
        ScienceQuestion(
            id = "ib1", domain = ScienceDomain.BIOLOGY,
            text = "Meiosis results in four genetically identical daughter cells?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Meiosis results in four genetically unique haploid cells.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ib2", domain = ScienceDomain.BIOLOGY,
            text = "Enzymes increase activation energy to speed up reactions?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Enzymes lower the activation energy.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ib3", domain = ScienceDomain.BIOLOGY,
            text = "The primary structure of a protein is its sequence of amino acids?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Primary structure is the linear chain of amino acids.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ib4", domain = ScienceDomain.BIOLOGY,
            text = "Stomata close during the night to prevent water loss?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Closing stomata reduces transpiration when photosynthesis is not occurring.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ib5", domain = ScienceDomain.BIOLOGY,
            text = "Active transport requires cellular energy (ATP)?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Active transport moves molecules against a concentration gradient using energy.",
            difficulty = "INTERMEDIATE"
        )
    )

    private fun getHindiQuestions() = listOf(
        // Physics - Basic 5
        ScienceQuestion(
            id = "p1", domain = ScienceDomain.PHYSICS,
            text = "वेग एक अदिश राशि है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "वेग एक सदिश राशि है क्योंकि इसमें दिशा शामिल होती है। गति इसका अदिश समकक्ष है।"
        ),
        ScienceQuestion(
            id = "p2", domain = ScienceDomain.PHYSICS,
            text = "न्यूटन के पहले नियम को जड़त्व का नियम भी कहा जाता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "जड़त्व वस्तुओं की अपनी गति की स्थिति में परिवर्तन का विरोध करने की प्रवृत्ति है।"
        ),
        ScienceQuestion(
            id = "p3", domain = ScienceDomain.PHYSICS,
            text = "ऊर्जा को शून्य से बनाया जा सकता है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "ऊर्जा संरक्षण का नियम कहता है कि ऊर्जा केवल एक रूप से दूसरे रूप में परिवर्तित होती है।"
        ),
        ScienceQuestion(
            id = "p4", domain = ScienceDomain.PHYSICS,
            text = "परावर्तन में, आपतन कोण परावर्तन कोण के बराबर होता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "यह प्रकाश के परावर्तन का मुख्य नियम है।"
        ),
        ScienceQuestion(
            id = "p5", domain = ScienceDomain.PHYSICS,
            text = "संवहन मुख्य रूप से ठोस पदार्थों में होता है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "संवहन के लिए तरल पदार्थ की गति की आवश्यकता होती है। ठोस चालन के माध्यम से ऊष्मा स्थानांतरित करते हैं।"
        ),
        // Chemistry - Basic 5
        ScienceQuestion(
            id = "c1", domain = ScienceDomain.CHEMISTRY,
            text = "गैसों का आयतन निश्चित होता है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "गैसें किसी भी कंटेनर को भरने के लिए फैलती हैं, उनका कोई निश्चित आकार या आयतन नहीं होता है।"
        ),
        ScienceQuestion(
            id = "c2", domain = ScienceDomain.CHEMISTRY,
            text = "एक मिश्रण रासायनिक रूप से बंधा होता है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "मिश्रण भौतिक संयोजन हैं; यौगिक रासायनिक संयोजन हैं।"
        ),
        ScienceQuestion(
            id = "c3", domain = ScienceDomain.CHEMISTRY,
            text = "परमाणु प्रोटॉन, न्यूट्रॉन और इलेक्ट्रॉन से बने होते हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "ये तीन मुख्य उप-परमाणु कण हैं।"
        ),
        ScienceQuestion(
            id = "c4", domain = ScienceDomain.CHEMISTRY,
            text = "लोहे में जंग लगना एक भौतिक परिवर्तन है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "जंग लगने से एक नया पदार्थ (आयरन ऑक्साइड) बनता है, जिससे यह एक रासायनिक परिवर्तन बन जाता है।"
        ),
        ScienceQuestion(
            id = "c5", domain = ScienceDomain.CHEMISTRY,
            text = "अम्ल नीले लिटमस पेपर को लाल कर देते हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "अम्ल स्वाद में खट्टे होते हैं और लिटमस को लाल कर देते हैं; क्षार इसे नीला कर देते हैं।"
        ),
        // Biology - Basic 5
        ScienceQuestion(
            id = "b1", domain = ScienceDomain.BIOLOGY,
            text = "पादप कोशिकाओं में कठोर कोशिका भित्ति होती है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "कोशिका भित्ति पादप कोशिकाओं के लिए संरचना और सुरक्षा प्रदान करती है।"
        ),
        ScienceQuestion(
            id = "b2", domain = ScienceDomain.BIOLOGY,
            text = "जाइलम पौधों में भोजन का परिवहन करता है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "जाइलम पानी का परिवहन करता है; फ्लोएम भोजन (ग्लूकोज) का परिवहन करता है।"
        ),
        ScienceQuestion(
            id = "b3", domain = ScienceDomain.BIOLOGY,
            text = "प्रकाश संश्लेषण माइटोकॉन्ड्रिया में होता है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "प्रकाश संश्लेषण क्लोरोप्लास्ट में होता है; माइटोकॉन्ड्रिया श्वसन के लिए हैं।"
        ),
        ScienceQuestion(
            id = "b4", domain = ScienceDomain.BIOLOGY,
            text = "हृदय संचार प्रणाली का हिस्सा है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "हृदय शरीर के परिवहन नेटवर्क के माध्यम से रक्त पंप करता है।"
        ),
        ScienceQuestion(
            id = "b5", domain = ScienceDomain.BIOLOGY,
            text = "जीन वंशानुगत जानकारी ले जाते हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "जीन माता-पिता से संतानों में जाने वाली आनुवंशिकता की इकाइयाँ हैं।"
        ),
        // Original Questions
        ScienceQuestion(
            id = "q1", domain = ScienceDomain.PHYSICS,
            text = "तरंगें ऊर्जा स्थानांतरित करती हैं, पदार्थ नहीं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "तरंगें माध्यम के माध्यम से ऊर्जा का प्रसार करती हैं बिना पदार्थ के स्थायी विस्थापन के।"
        ),
        ScienceQuestion(
            id = "q2", domain = ScienceDomain.PHYSICS,
            text = "बल द्रव्यमान गुणा त्वरण है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "न्यूटन का दूसरा नियम कहता है F = m × a।"
        ),
        ScienceQuestion(
            id = "q3", domain = ScienceDomain.CHEMISTRY,
            text = "बोहर मॉडल हाइड्रोजन-जैसे परमाणुओं के लिए है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "बोहर मॉडल एकल-इलेक्ट्रॉन प्रणालियों जैसे हाइड्रोजन, He+, Li2+ आदि की सटीक व्याख्या करता है।"
        ),
        ScienceQuestion(
            id = "q4", domain = ScienceDomain.BIOLOGY,
            text = "पशु कोशिकाओं में बड़ी रिक्तिकाएं होती हैं?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "पादप कोशिकाओं में भंडारण और सहायता के लिए बड़ी केंद्रीय रिक्तिकाएं होती हैं।"
        ),
        ScienceQuestion(
            id = "q5", domain = ScienceDomain.CHEMISTRY,
            text = "आइसोटोप में न्यूट्रॉन की संख्या समान होती है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "आइसोटोप में प्रोटॉन की संख्या समान होती है लेकिन न्यूट्रॉन की संख्या भिन्न होती है।"
        ),
        ScienceQuestion(
            id = "q6", domain = ScienceDomain.PHYSICS,
            text = "प्रकाश हमेशा स्थिर गति से यात्रा करता है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "प्रकाश की गति माध्यम के अनुसार बदलती है।"
        ),
        ScienceQuestion(
            id = "q7", domain = ScienceDomain.BIOLOGY,
            text = "DNA प्रतिकृति अर्ध-संरक्षी है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "प्रत्येक नए DNA अणु में एक मूल स्ट्रैंड और एक नया संश्लेषित स्ट्रैंड होता है।"
        ),
        ScienceQuestion(
            id = "q8", domain = ScienceDomain.PHYSICS,
            text = "गतिज ऊर्जा वेग के वर्ग पर निर्भर करती है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "KE = ½mv²। वेग को दोगुना करने से गतिज ऊर्जा चार गुना हो जाती है।"
        ),
        ScienceQuestion(
            id = "q9", domain = ScienceDomain.CHEMISTRY,
            text = "इलेक्ट्रॉन ग्रहों की तरह नाभिक की परिक्रमा करते हैं?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "इलेक्ट्रॉन प्रायिकता बादलों में मौजूद होते हैं, निश्चित कक्षाओं में नहीं।"
        ),
        ScienceQuestion(
            id = "q10", domain = ScienceDomain.BIOLOGY,
            text = "मािटोकॉन्ड्रिया केवल ATP उत्पन्न करते हैं?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "ATP संश्लेषण के साथ, माइटोकॉन्ड्रिया अन्य कोशिकीय कार्यों को भी नियंत्रित करते हैं।"
        ),
        // INTERMEDIATE QUESTIONS - Physics
        ScienceQuestion(
            id = "ip1", domain = ScienceDomain.PHYSICS,
            text = "पृथ्वी से पलायन वेग वस्तु के द्रव्यमान पर निर्भर नहीं करता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "यह केवल ग्रह के द्रव्यमान और त्रिज्या पर निर्भर करता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ip2", domain = ScienceDomain.PHYSICS,
            text = "एक अलग प्रणाली की एन्ट्रॉपी समय के साथ घट सकती है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "ऊष्मप्रवैगिकी का दूसरा नियम कहता है कि एन्ट्रॉपी हमेशा बढ़ती है या स्थिर रहती है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ip3", domain = ScienceDomain.PHYSICS,
            text = "एक p-प्रकार के अर्धचालक में बहुसंख्यक वाहक के रूप में छिद्र (holes) होते हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "P-प्रकार के अर्धचालकों को छिद्रों की अधिकता बनाने के लिए डोप किया जाता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ip4", domain = ScienceDomain.PHYSICS,
            text = "लेंज का नियम ऊर्जा संरक्षण के नियम का परिणाम है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "प्रेरित धारा ऊर्जा बचाने के लिए परिवर्तन का विरोध करती है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ip5", domain = ScienceDomain.PHYSICS,
            text = "ध्वनि की गति हवा की तुलना में पानी में तेज होती है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "ध्वनि सघन माध्यमों में तेजी से यात्रा करती है।",
            difficulty = "INTERMEDIATE"
        ),
        // INTERMEDIATE QUESTIONS - Chemistry
        ScienceQuestion(
            id = "ic1", domain = ScienceDomain.CHEMISTRY,
            text = "महान गैसों की अपनी अवधि में उच्चतम वैद्युतीयऋणात्मकता होती है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "महान गैसों की वैद्युतीयऋणात्मकता आम तौर पर शून्य होती है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ic2", domain = ScienceDomain.CHEMISTRY,
            text = "एक बफर समाधान पीएच में परिवर्तन का प्रतिरोध करता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "बफर में एसिड और बेस दोनों होते हैं जो बाहरी प्रभाव को बेअसर करते हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ic3", domain = ScienceDomain.CHEMISTRY,
            text = "एथीन (C2H4) में कार्बन का संकरण sp3 है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "एथीन में कार्बन sp2 संकरित होता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ic4", domain = ScienceDomain.CHEMISTRY,
            text = "एंडोथर्मिक प्रतिक्रियाओं में एन्थैल्पी (ΔH) में नकारात्मक परिवर्तन होता है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "एंडोथर्मिक प्रतिक्रियाएं गर्मी सोखती हैं, इसलिए ΔH सकारात्मक होता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ic5", domain = ScienceDomain.CHEMISTRY,
            text = "सक्रियण ऊर्जा प्रतिक्रिया शुरू करने के लिए आवश्यक न्यूनतम ऊर्जा है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "यह वह ऊर्जा बाधा है जिसे प्रतिक्रिया शुरू करने के लिए पार करना होगा।",
            difficulty = "INTERMEDIATE"
        ),
        // INTERMEDIATE QUESTIONS - Biology
        ScienceQuestion(
            id = "ib1", domain = ScienceDomain.BIOLOGY,
            text = "अर्धसूत्रीविभाजन के परिणामस्वरूप चार आनुवंशिक रूप से समान कोशिकाएं बनती हैं?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "अर्धसूत्रीविभाजन से चार अद्वितीय अगुणित कोशिकाएं बनती हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ib2", domain = ScienceDomain.BIOLOGY,
            text = "एंजाइम प्रतिक्रियाओं को तेज करने के लिए सक्रियण ऊर्जा बढ़ाते हैं?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "एंजाइम सक्रियण ऊर्जा को कम करते हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ib3", domain = ScienceDomain.BIOLOGY,
            text = "प्रोटीन की प्राथमिक संरचना अमीनो एसिड का अनुक्रम है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "प्राथमिक संरचना अमीनो एसिड की रैखिक श्रृंखला है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ib4", domain = ScienceDomain.BIOLOGY,
            text = "पानी की कमी को रोकने के लिए रात में रंध्र (stomata) बंद हो जाते हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "रंध्र बंद होने से वाष्पोत्सर्जन कम हो जाता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "ib5", domain = ScienceDomain.BIOLOGY,
            text = "सक्रिय परिवहन के लिए कोशिकीय ऊर्जा (ATP) की आवश्यकता होती है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "सक्रिय परिवहन ऊर्जा का उपयोग करके अणुओं को उच्च सांद्रता की ओर ले जाता है।",
            difficulty = "INTERMEDIATE"
        )
    )
}
