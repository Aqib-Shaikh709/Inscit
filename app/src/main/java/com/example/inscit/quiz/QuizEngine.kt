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

    fun getDailyRoundQuestions(round: Int, lang: Lang): List<ScienceQuestion> {
        return when (round) {
            1 -> if (lang == Lang.HI) getRoundOneHindi() else getRoundOneEnglish()
            2 -> if (lang == Lang.HI) getRoundTwoHindi() else getRoundTwoEnglish()
            3 -> if (lang == Lang.HI) getRoundThreeHindi() else getRoundThreeEnglish()
            else -> emptyList()
        }
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
        val averageEn = radarData.filter { it.score in 0.5f..<0.8f }.map { it.domain.displayNameEn }
        val averageHi = radarData.filter { it.score in 0.5f..<0.8f }.map { it.domain.displayNameHi }
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
            averageEn = averageEn,
            averageHi = averageHi,
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

    // ============ REGULAR QUIZ BANK (rooted in app syllabus, one per topic) ============

    private fun getEnglishQuestions() = listOf(
        ScienceQuestion(
            id = "reg_p1", domain = ScienceDomain.PHYSICS,
            text = "Displacement is the shortest straight-line distance between the starting and ending points of motion?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Distance measures the total ground covered, while displacement measures the direct change in position."
        ),
        ScienceQuestion(
            id = "reg_p2", domain = ScienceDomain.PHYSICS,
            text = "Newton's First Law of Motion is also known as the Law of Inertia?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Inertia is the tendency of objects to resist changes in their state of motion."
        ),
        ScienceQuestion(
            id = "reg_p3", domain = ScienceDomain.PHYSICS,
            text = "Kinetic energy is the energy of motion, while potential energy is stored energy?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "A moving object has kinetic energy; a stretched rubber band or a rock on a cliff stores potential energy."
        ),
        ScienceQuestion(
            id = "reg_p4", domain = ScienceDomain.PHYSICS,
            text = "According to the Law of Reflection, the angle of incidence equals the angle of reflection?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "This is why you see a clear image of yourself in a flat mirror."
        ),
        ScienceQuestion(
            id = "reg_p5", domain = ScienceDomain.PHYSICS,
            text = "Heat travels through a metal spoon mainly by convection?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Metals conduct heat directly through particle collisions — that is conduction, not convection.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_p6", domain = ScienceDomain.PHYSICS,
            text = "Transverse waves vibrate perpendicular to the direction in which the energy travels?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Light and waves on a guitar string are transverse; sound waves are longitudinal.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_p7", domain = ScienceDomain.PHYSICS,
            text = "A changing magnetic field can create an electric current?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "This electromagnetic induction is what powers the generators of our modern world.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_p8", domain = ScienceDomain.PHYSICS,
            text = "Fusion is the splitting of a heavy nucleus like uranium into smaller pieces?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Splitting a heavy nucleus is fission; fusion joins tiny nuclei together and powers the Sun.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_c1", domain = ScienceDomain.CHEMISTRY,
            text = "Gases have no fixed shape or volume because their particles move freely?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Gases expand to fill whatever container they are placed in."
        ),
        ScienceQuestion(
            id = "reg_c2", domain = ScienceDomain.CHEMISTRY,
            text = "A mixture is a physical combination of substances, not a chemical bond?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Compounds, unlike mixtures, are chemically bonded in fixed proportions."
        ),
        ScienceQuestion(
            id = "reg_c3", domain = ScienceDomain.CHEMISTRY,
            text = "The nucleus is the dense central core of an atom made of protons and neutrons?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Protons and neutrons form the nucleus; electrons orbit outside it."
        ),
        ScienceQuestion(
            id = "reg_c4", domain = ScienceDomain.CHEMISTRY,
            text = "Rusting of iron is a physical change because no new substance is formed?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Rusting forms iron oxide, a brand new substance — so it is a chemical change."
        ),
        ScienceQuestion(
            id = "reg_c5", domain = ScienceDomain.CHEMISTRY,
            text = "Bases taste bitter and turn red litmus paper blue?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Acids are sour and turn blue litmus red; bases do the opposite.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_c6", domain = ScienceDomain.CHEMISTRY,
            text = "In quantum theory, electrons exist in probability clouds (orbitals) rather than fixed orbits?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "The Bohr model only works well for single-electron systems like hydrogen.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_c7", domain = ScienceDomain.CHEMISTRY,
            text = "Electronegativity generally increases from left to right across a period?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Noble gases are the exception, having near-zero electronegativity.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_c8", domain = ScienceDomain.CHEMISTRY,
            text = "Ionic bonds form when atoms share pairs of electrons?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Sharing electrons creates covalent bonds; ionic bonds form by electron transfer.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_b1", domain = ScienceDomain.BIOLOGY,
            text = "Plant cells have a rigid cell wall that provides structure and protection?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Animal cells do not have a cell wall."
        ),
        ScienceQuestion(
            id = "reg_b2", domain = ScienceDomain.BIOLOGY,
            text = "Phloem transports the food made in the leaves to the rest of the plant?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Xylem carries water and minerals upward; phloem carries food downward and around."
        ),
        ScienceQuestion(
            id = "reg_b3", domain = ScienceDomain.BIOLOGY,
            text = "Photosynthesis occurs in the chloroplasts of plant cells?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Chloroplasts trap sunlight; mitochondria are the site of respiration."
        ),
        ScienceQuestion(
            id = "reg_b4", domain = ScienceDomain.BIOLOGY,
            text = "Cellular respiration takes place mainly in the mitochondria and produces ATP?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Mitochondria are known as the powerhouse of the cell."
        ),
        ScienceQuestion(
            id = "reg_b5", domain = ScienceDomain.BIOLOGY,
            text = "DNA replication is semi-conservative: each new molecule has one old and one new strand?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "This preserves genetic information across generations.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_b6", domain = ScienceDomain.BIOLOGY,
            text = "Hormones travel through the blood to reach their target organs?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Endocrine glands release these chemical messengers into the bloodstream.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_b7", domain = ScienceDomain.BIOLOGY,
            text = "Reflex actions are slow, conscious decisions made by the brain?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Reflexes are rapid, automatic responses often controlled by the spinal cord.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_b8", domain = ScienceDomain.BIOLOGY,
            text = "In a food chain, energy flows from producers to consumers?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Plants (producers) capture sunlight energy that animals (consumers) then use.",
            difficulty = "INTERMEDIATE"
        )
    )

    private fun getHindiQuestions() = listOf(
        ScienceQuestion(
            id = "reg_p1", domain = ScienceDomain.PHYSICS,
            text = "विस्थापन गति के शुरुआती और अंतिम बिंदुओं के बीच की सबसे छोटी सीधी दूरी है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "दूरी तय किया गया कुल रास्ता है, जबकि विस्थापन स्थिति में सीधा बदलाव मापता है।"
        ),
        ScienceQuestion(
            id = "reg_p2", domain = ScienceDomain.PHYSICS,
            text = "न्यूटन के गति के पहले नियम को जड़त्व का नियम भी कहा जाता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "जड़त्व वस्तुओं की अपनी गति की स्थिति में बदलाव का विरोध करने की प्रवृत्ति है।"
        ),
        ScienceQuestion(
            id = "reg_p3", domain = ScienceDomain.PHYSICS,
            text = "गतिज ऊर्जा गति की ऊर्जा है, जबकि स्थितिज ऊर्जा संग्रहीत ऊर्जा है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "चलती वस्तु में गतिज ऊर्जा होती है; खिंचा रबर बैंड या चट्टान पर पत्थर स्थितिज ऊर्जा संग्रहीत करता है।"
        ),
        ScienceQuestion(
            id = "reg_p4", domain = ScienceDomain.PHYSICS,
            text = "परावर्तन के नियम के अनुसार, आपतन कोण परावर्तन कोण के बराबर होता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "यही कारण है कि सपाट दर्पण में आप अपनी स्पष्ट छवि देखते हैं।"
        ),
        ScienceQuestion(
            id = "reg_p5", domain = ScienceDomain.PHYSICS,
            text = "धातु के चम्मच में ऊष्मा मुख्य रूप से संवहन द्वारा चलती है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "धातुएँ कणों के टकराव से सीधे ऊष्मा का चालन करती हैं — यह चालन है, संवहन नहीं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_p6", domain = ScienceDomain.PHYSICS,
            text = "अनुप्रस्थ तरंगें ऊर्जा के यात्रा करने की दिशा के लंबवत कंपन करती हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "प्रकाश और गिटार के तार की तरंगें अनुप्रस्थ हैं; ध्वनि तरंगें अनुदैर्ध्य हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_p7", domain = ScienceDomain.PHYSICS,
            text = "बदलता चुंबकीय क्षेत्र विद्युत धारा उत्पन्न कर सकता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "यह विद्युत चुम्बकीय प्रेरण हमारी आधुनिक दुनिया के जनरेटरों को शक्ति देता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_p8", domain = ScienceDomain.PHYSICS,
            text = "संलयन यूरेनियम जैसे भारी नाभिक के छोटे टुकड़ों में टूटने की प्रक्रिया है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "भारी नाभिक का टूटना विखंडन है; संलयन छोटे नाभिकों को जोड़ता है और सूर्य को शक्ति देता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_c1", domain = ScienceDomain.CHEMISTRY,
            text = "गैसों का कोई निश्चित आकार या आयतन नहीं होता क्योंकि उनके कण स्वतंत्र रूप से चलते हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "गैसें जिस भी कंटेनर में रखी जाती हैं उसे भरने के लिए फैलती हैं।"
        ),
        ScienceQuestion(
            id = "reg_c2", domain = ScienceDomain.CHEMISTRY,
            text = "मिश्रण पदार्थों का भौतिक संयोजन है, रासायनिक बंधन नहीं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "यौगिक, मिश्रण के विपरीत, निश्चित अनुपात में रासायनिक रूप से बंधे होते हैं।"
        ),
        ScienceQuestion(
            id = "reg_c3", domain = ScienceDomain.CHEMISTRY,
            text = "नाभिक परमाणु का घना केंद्रीय भाग है जो प्रोटॉन और न्यूट्रॉन से बना होता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "प्रोटॉन और न्यूट्रॉन नाभिक बनाते हैं; इलेक्ट्रॉन इसके बाहर परिक्रमा करते हैं।"
        ),
        ScienceQuestion(
            id = "reg_c4", domain = ScienceDomain.CHEMISTRY,
            text = "लोहे में जंग लगना एक भौतिक परिवर्तन है क्योंकि कोई नया पदार्थ नहीं बनता?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "जंग लगने से आयरन ऑक्साइड बनता है, जो एक बिल्कुल नया पदार्थ है — इसलिए यह रासायनिक परिवर्तन है।"
        ),
        ScienceQuestion(
            id = "reg_c5", domain = ScienceDomain.CHEMISTRY,
            text = "क्षार स्वाद में कड़वे होते हैं और लाल लिटमस पेपर को नीला कर देते हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "अम्ल खट्टे होते हैं और नीले लिटमस को लाल करते हैं; क्षार इसके विपरीत करते हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_c6", domain = ScienceDomain.CHEMISTRY,
            text = "क्वांटम सिद्धांत में इलेक्ट्रॉन निश्चित कक्षाओं के बजाय प्रायिकता बादलों (ऑर्बिटल्स) में मौजूद होते हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "बोहर मॉडल केवल हाइड्रोजन जैसी एकल-इलेक्ट्रॉन प्रणालियों के लिए अच्छा काम करता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_c7", domain = ScienceDomain.CHEMISTRY,
            text = "वैद्युतीयऋणात्मकता आवर्त में बाएँ से दाएँ जाने पर आम तौर पर बढ़ती है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "महान गैसें अपवाद हैं, जिनकी वैद्युतीयऋणात्मकता लगभग शून्य होती है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_c8", domain = ScienceDomain.CHEMISTRY,
            text = "आयनिक बंध इलेक्ट्रॉनों के जोड़े साझा करने से बनते हैं?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "इलेक्ट्रॉन साझा करने से सहसंयोजक बंध बनते हैं; आयनिक बंध इलेक्ट्रॉन स्थानांतरण से बनते हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_b1", domain = ScienceDomain.BIOLOGY,
            text = "पादप कोशिकाओं में कठोर कोशिका भित्ति होती है जो संरचना और सुरक्षा प्रदान करती है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "पशु कोशिकाओं में कोशिका भित्ति नहीं होती।"
        ),
        ScienceQuestion(
            id = "reg_b2", domain = ScienceDomain.BIOLOGY,
            text = "फ्लोएम पत्तियों में बने भोजन को पौधे के बाकी हिस्सों तक पहुँचाता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "जाइलम पानी और खनिज ऊपर ले जाता है; फ्लोएम भोजन ले जाता है।"
        ),
        ScienceQuestion(
            id = "reg_b3", domain = ScienceDomain.BIOLOGY,
            text = "प्रकाश संश्लेषण पादप कोशिकाओं के क्लोरोप्लास्ट में होता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "क्लोरोप्लास्ट सूर्यप्रकाश पकड़ते हैं; माइटोकॉन्ड्रिया श्वसन का स्थान है।"
        ),
        ScienceQuestion(
            id = "reg_b4", domain = ScienceDomain.BIOLOGY,
            text = "कोशिकीय श्वसन मुख्य रूप से माइटोकॉन्ड्रिया में होता है और ATP उत्पन्न करता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "माइटोकॉन्ड्रिया को कोशिका का ऊर्जा घर कहा जाता है।"
        ),
        ScienceQuestion(
            id = "reg_b5", domain = ScienceDomain.BIOLOGY,
            text = "DNA प्रतिकृति अर्ध-संरक्षी है: प्रत्येक नए अणु में एक पुरानी और एक नई श्रृंखला होती है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "यह आनुवंशिक जानकारी को पीढ़ियों तक सुरक्षित रखता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_b6", domain = ScienceDomain.BIOLOGY,
            text = "हार्मोन रक्त के माध्यम से अपने लक्ष्य अंगों तक पहुँचते हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "अंतःस्रावी ग्रंथियाँ ये रासायनिक संदेशवाहक रक्तप्रवाह में छोड़ती हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_b7", domain = ScienceDomain.BIOLOGY,
            text = "प्रतिवर्ती क्रियाएँ मस्तिष्क द्वारा किए गए धीमे, सचेत निर्णय हैं?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "प्रतिवर्ती तीव्र, स्वचालित प्रतिक्रियाएँ हैं जो अक्सर मेरुदंड द्वारा नियंत्रित होती हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "reg_b8", domain = ScienceDomain.BIOLOGY,
            text = "खाद्य श्रृंखला में ऊर्जा उत्पादकों से उपभोक्ताओं की ओर बहती है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "पौधे (उत्पादक) सूर्यप्रकाश की ऊर्जा पकड़ते हैं जिसका उपयोग जानवर (उपभोक्ता) करते हैं।",
            difficulty = "INTERMEDIATE"
        )
    )

    // ============ DAILY CHALLENGE ROUND 1 (5 questions) ============

    private fun getRoundOneEnglish() = listOf(
        ScienceQuestion(
            id = "r1_1", domain = ScienceDomain.PHYSICS,
            text = "The area under a velocity-time graph represents the distance travelled?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Motion graphs act like maps — the shapes on a velocity graph tell you how far an object travelled."
        ),
        ScienceQuestion(
            id = "r1_2", domain = ScienceDomain.CHEMISTRY,
            text = "Gases have a fixed shape and a fixed volume?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Gas particles move freely, so gases take the shape and volume of whatever container holds them."
        ),
        ScienceQuestion(
            id = "r1_3", domain = ScienceDomain.BIOLOGY,
            text = "Plant cells have a large central vacuole that animal cells do not have?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "The large central vacuole stores water and supports the plant cell."
        ),
        ScienceQuestion(
            id = "r1_4", domain = ScienceDomain.PHYSICS,
            text = "A convex mirror is used as a parking-lot safety mirror because it spreads light out?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "The bulging mirror makes everything look smaller and shows a much wider view."
        ),
        ScienceQuestion(
            id = "r1_5", domain = ScienceDomain.CHEMISTRY,
            text = "Acids are sour in taste and turn blue litmus paper red?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Acids are sour and turn litmus red; bases are bitter and turn it blue."
        )
    )

    private fun getRoundOneHindi() = listOf(
        ScienceQuestion(
            id = "r1_1", domain = ScienceDomain.PHYSICS,
            text = "वेग-समय ग्राफ के नीचे का क्षेत्रफल तय की गई दूरी को दर्शाता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "गति के ग्राफ नक्शे की तरह काम करते हैं — वेग ग्राफ की आकृतियाँ बताती हैं कि वस्तु ने कितनी दूरी तय की।"
        ),
        ScienceQuestion(
            id = "r1_2", domain = ScienceDomain.CHEMISTRY,
            text = "गैसों का एक निश्चित आकार और एक निश्चित आयतन होता है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "गैस के कण स्वतंत्र रूप से चलते हैं, इसलिए गैसें अपने कंटेनर का आकार और आयतन ले लेती हैं।"
        ),
        ScienceQuestion(
            id = "r1_3", domain = ScienceDomain.BIOLOGY,
            text = "पादप कोशिकाओं में बड़ी केंद्रीय रिक्तिका होती है जो पशु कोशिकाओं में नहीं होती?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "बड़ी केंद्रीय रिक्तिका पानी संग्रहीत करती है और पादप कोशिका को सहारा देती है।"
        ),
        ScienceQuestion(
            id = "r1_4", domain = ScienceDomain.PHYSICS,
            text = "उत्तल दर्पण पार्किंग स्थल के सुरक्षा दर्पण के रूप में उपयोग होता है क्योंकि यह प्रकाश को फैलाता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "उभरा हुआ दर्पण सब कुछ छोटा दिखाता है और बहुत व्यापक दृश्य दिखाता है।"
        ),
        ScienceQuestion(
            id = "r1_5", domain = ScienceDomain.CHEMISTRY,
            text = "अम्ल स्वाद में खट्टे होते हैं और नीले लिटमस पेपर को लाल कर देते हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "अम्ल खट्टे होते हैं और लिटमस को लाल करते हैं; क्षार कड़वे होते हैं और इसे नीला करते हैं।"
        )
    )

    // ============ DAILY CHALLENGE ROUND 2 (10 questions) ============

    private fun getRoundTwoEnglish() = listOf(
        ScienceQuestion(
            id = "r2_1", domain = ScienceDomain.PHYSICS,
            text = "Pushing hard against a wall without moving it is zero work in physics?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Work requires force AND movement in the direction of the force."
        ),
        ScienceQuestion(
            id = "r2_2", domain = ScienceDomain.CHEMISTRY,
            text = "Compounds are formed by physical mixing, not by chemical bonding?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Compounds are chemical combinations with fixed proportions; mixtures are physical."
        ),
        ScienceQuestion(
            id = "r2_3", domain = ScienceDomain.BIOLOGY,
            text = "Xylem transports water, and phloem transports food in plants?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "The two transport tissues divide their duties between water and food."
        ),
        ScienceQuestion(
            id = "r2_4", domain = ScienceDomain.PHYSICS,
            text = "Radiation is the only way heat can travel through the vacuum of space?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "The Sun's heat reaches Earth across empty space as electromagnetic waves."
        ),
        ScienceQuestion(
            id = "r2_5", domain = ScienceDomain.CHEMISTRY,
            text = "Carbon in ethene (C2H4) is sp2 hybridized because of its double bond?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Each double-bonded carbon forms three sigma bonds and one pi bond.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r2_6", domain = ScienceDomain.BIOLOGY,
            text = "Meiosis produces four genetically unique haploid cells?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "This creates the genetic variety seen in sperm and egg cells.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r2_7", domain = ScienceDomain.PHYSICS,
            text = "Sound waves are longitudinal waves that create compressions and rarefactions?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Particles vibrate back and forth in the same direction the sound travels."
        ),
        ScienceQuestion(
            id = "r2_8", domain = ScienceDomain.CHEMISTRY,
            text = "Noble gases have the highest electronegativity in their period?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Noble gases have full electron shells and near-zero electronegativity.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r2_9", domain = ScienceDomain.BIOLOGY,
            text = "Respiration happens mainly in the mitochondria, known as the powerhouse of the cell?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "This is where glucose is broken down to produce ATP."
        ),
        ScienceQuestion(
            id = "r2_10", domain = ScienceDomain.PHYSICS,
            text = "An electric current flowing through a wire turns it into a temporary electromagnet?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "We can control the strength of such magnets by changing the current."
        )
    )

    private fun getRoundTwoHindi() = listOf(
        ScienceQuestion(
            id = "r2_1", domain = ScienceDomain.PHYSICS,
            text = "दीवार को हिलाए बिना उसे जोर से धकेलना भौतिकी में शून्य कार्य है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "कार्य के लिए बल और बल की दिशा में गति दोनों आवश्यक हैं।"
        ),
        ScienceQuestion(
            id = "r2_2", domain = ScienceDomain.CHEMISTRY,
            text = "यौगिक रासायनिक बंधन से नहीं, भौतिक मिश्रण से बनते हैं?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "यौगिक निश्चित अनुपात में रासायनिक संयोजन होते हैं; मिश्रण भौतिक होते हैं।"
        ),
        ScienceQuestion(
            id = "r2_3", domain = ScienceDomain.BIOLOGY,
            text = "पौधों में जाइलम पानी का परिवहन करता है और फ्लोएम भोजन का?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "दोनों परिवहन ऊतक पानी और भोजन के बीच अपने कर्तव्य बाँटते हैं।"
        ),
        ScienceQuestion(
            id = "r2_4", domain = ScienceDomain.PHYSICS,
            text = "विकिरण अंतरिक्ष के निर्वात से ऊष्मा ले जाने का एकमात्र तरीका है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "सूर्य की गर्मी विद्युत चुम्बकीय तरंगों के रूप में खाली अंतरिक्ष पार करके पृथ्वी तक पहुँचती है।"
        ),
        ScienceQuestion(
            id = "r2_5", domain = ScienceDomain.CHEMISTRY,
            text = "एथीन (C2H4) में कार्बन अपने द्विबंध के कारण sp2 संकरित है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "प्रत्येक द्विबंधित कार्बन तीन सिग्मा और एक पाई बंध बनाता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r2_6", domain = ScienceDomain.BIOLOGY,
            text = "अर्धसूत्रीविभाजन चार आनुवंशिक रूप से अद्वितीय अगुणित कोशिकाएँ बनाता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "यह शुक्राणु और अंडाणु कोशिकाओं में दिखने वाली आनुवंशिक विविधता पैदा करता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r2_7", domain = ScienceDomain.PHYSICS,
            text = "ध्वनि तरंगें अनुदैर्ध्य तरंगें हैं जो संपीडन और विरलन बनाती हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "कण उसी दिशा में आगे-पीछे कंपन करते हैं जिस दिशा में ध्वनि यात्रा करती है।"
        ),
        ScienceQuestion(
            id = "r2_8", domain = ScienceDomain.CHEMISTRY,
            text = "महान गैसों की अपनी आवर्त में उच्चतम वैद्युतीयऋणात्मकता होती है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "महान गैसों के इलेक्ट्रॉन कोश पूर्ण होते हैं और उनकी वैद्युतीयऋणात्मकता लगभग शून्य होती है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r2_9", domain = ScienceDomain.BIOLOGY,
            text = "श्वसन मुख्य रूप से माइटोकॉन्ड्रिया में होता है, जिसे कोशिका का ऊर्जा घर कहा जाता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "यहाँ ग्लूकोज तोड़कर ATP का उत्पादन किया जाता है।"
        ),
        ScienceQuestion(
            id = "r2_10", domain = ScienceDomain.PHYSICS,
            text = "तार में बहने वाली विद्युत धारा उसे अस्थायी विद्युत चुम्बक बना देती है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "धारा बदलकर हम ऐसे चुम्बकों की शक्ति नियंत्रित कर सकते हैं।"
        )
    )

    // ============ DAILY CHALLENGE ROUND 3 (15 questions) ============

    private fun getRoundThreeEnglish() = listOf(
        ScienceQuestion(
            id = "r3_1", domain = ScienceDomain.PHYSICS,
            text = "The strong nuclear force overcomes the electrical repulsion between protons to hold the nucleus together?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "It is the most powerful force in nature, but it only works at incredibly tiny distances.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_2", domain = ScienceDomain.CHEMISTRY,
            text = "Electrons orbit the nucleus in fixed circular paths like planets around the Sun?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Quantum theory says electrons live in probability clouds (orbitals), not fixed orbits.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_3", domain = ScienceDomain.BIOLOGY,
            text = "Enzymes speed up reactions by raising the activation energy?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Enzymes LOWER the activation energy barrier to make reactions faster.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_4", domain = ScienceDomain.PHYSICS,
            text = "From F = ma, doubling the mass halves the acceleration when force is constant?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Heavier objects are much harder to speed up than light ones.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_5", domain = ScienceDomain.CHEMISTRY,
            text = "Rusting of iron is a chemical change because a new substance (iron oxide) is formed?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Chemical changes always produce brand new substances.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_6", domain = ScienceDomain.BIOLOGY,
            text = "The endocrine system sends chemical signals called hormones through the blood?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Hormones regulate growth, metabolism, and reproduction.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_7", domain = ScienceDomain.PHYSICS,
            text = "Acceleration measures how quickly velocity changes over time?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Speeding up, slowing down, or changing direction all involve acceleration.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_8", domain = ScienceDomain.CHEMISTRY,
            text = "Isotopes of the same element contain the same number of neutrons?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "Isotopes share the same proton count but differ in their neutron counts.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_9", domain = ScienceDomain.BIOLOGY,
            text = "In a food chain, producers like green plants capture sunlight through photosynthesis?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Producers form the base of every food chain.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_10", domain = ScienceDomain.PHYSICS,
            text = "A concave mirror can form a real image that is projected onto a screen?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "It focuses light to a single point — the 'soup spoon' side of a curved mirror.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_11", domain = ScienceDomain.CHEMISTRY,
            text = "A buffer solution resists changes in pH when small amounts of acid or base are added?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Buffers contain a weak acid/base pair that neutralizes small additions.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_12", domain = ScienceDomain.BIOLOGY,
            text = "Photosynthesis in plant cells happens in the mitochondria?",
            options = listOf(QuizOption(1, "True", false), QuizOption(2, "False", true)),
            explanation = "It happens in the chloroplasts; mitochondria handle respiration.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_13", domain = ScienceDomain.PHYSICS,
            text = "Maxwell's equations predicted electromagnetic waves that travel at the speed of light?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "Radio waves, visible light, and X-rays are all electromagnetic waves.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_14", domain = ScienceDomain.CHEMISTRY,
            text = "Solids have a definite shape because their particles vibrate around fixed positions?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "In liquids and gases, particles are free to move much more.",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_15", domain = ScienceDomain.BIOLOGY,
            text = "Reflex actions are rapid, automatic responses that often bypass conscious thought?",
            options = listOf(QuizOption(1, "True", true), QuizOption(2, "False", false)),
            explanation = "The spinal cord handles many reflexes before the brain even knows.",
            difficulty = "INTERMEDIATE"
        )
    )

    private fun getRoundThreeHindi() = listOf(
        ScienceQuestion(
            id = "r3_1", domain = ScienceDomain.PHYSICS,
            text = "प्रबल नाभिकीय बल प्रोटॉनों के बीच विद्युत प्रतिकर्षण को पार करके नाभिक को जोड़े रखता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "यह प्रकृति का सबसे शक्तिशाली बल है, लेकिन यह केवल अत्यंत सूक्ष्म दूरी पर काम करता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_2", domain = ScienceDomain.CHEMISTRY,
            text = "इलेक्ट्रॉन सूर्य के चारों ओर ग्रहों की तरह निश्चित वृत्ताकार कक्षाओं में नाभिक की परिक्रमा करते हैं?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "क्वांटम सिद्धांत कहता है कि इलेक्ट्रॉन प्रायिकता बादलों (ऑर्बिटल्स) में रहते हैं, निश्चित कक्षाओं में नहीं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_3", domain = ScienceDomain.BIOLOGY,
            text = "एंजाइम सक्रियण ऊर्जा बढ़ाकर प्रतिक्रियाओं को तेज़ करते हैं?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "एंजाइम सक्रियण ऊर्जा की बाधा को कम करते हैं, जिससे प्रतिक्रियाएँ तेज़ हो जाती हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_4", domain = ScienceDomain.PHYSICS,
            text = "F = ma से, स्थिर बल पर द्रव्यमान दोगुना करने पर त्वरण आधा हो जाता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "भारी वस्तुओं को हल्की वस्तुओं की तुलना में तेज़ करना बहुत कठिन होता है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_5", domain = ScienceDomain.CHEMISTRY,
            text = "लोहे में जंग लगना एक रासायनिक परिवर्तन है क्योंकि एक नया पदार्थ (आयरन ऑक्साइड) बनता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "रासायनिक परिवर्तन हमेशा बिल्कुल नए पदार्थ बनाते हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_6", domain = ScienceDomain.BIOLOGY,
            text = "अंतःस्रावी तंत्र हार्मोन नामक रासायनिक संकेत रक्त के माध्यम से भेजता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "हार्मोन विकास, चयापचय और प्रजनन को नियंत्रित करते हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_7", domain = ScienceDomain.PHYSICS,
            text = "त्वरण यह मापता है कि वेग समय के साथ कितनी तेज़ी से बदलता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "तेज़ होना, धीमा होना या दिशा बदलना — सभी में त्वरण शामिल है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_8", domain = ScienceDomain.CHEMISTRY,
            text = "किसी तत्व के समस्थानिकों में न्यूट्रॉन की संख्या समान होती है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "समस्थानिकों में प्रोटॉन की संख्या समान होती है लेकिन न्यूट्रॉन की संख्या भिन्न होती है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_9", domain = ScienceDomain.BIOLOGY,
            text = "खाद्य श्रृंखला में हरे पौधे जैसे उत्पादक प्रकाश संश्लेषण से सूर्यप्रकाश पकड़ते हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "उत्पादक हर खाद्य श्रृंखला का आधार होते हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_10", domain = ScienceDomain.PHYSICS,
            text = "अवतल दर्पण एक वास्तविक छवि बना सकता है जिसे स्क्रीन पर दिखाया जा सकता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "यह प्रकाश को एक बिंदु पर केंद्रित करता है — घुमावदार दर्पण का 'सूप चम्मच' वाला भाग।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_11", domain = ScienceDomain.CHEMISTRY,
            text = "बफर घोल थोड़ी मात्रा में अम्ल या क्षार मिलाने पर pH में परिवर्तन का प्रतिरोध करता है?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "बफर में दुर्बल अम्ल/क्षार की जोड़ी होती है जो छोटे प्रभावों को बेअसर कर देती है।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_12", domain = ScienceDomain.BIOLOGY,
            text = "पादप कोशिकाओं में प्रकाश संश्लेषण माइटोकॉन्ड्रिया में होता है?",
            options = listOf(QuizOption(1, "सही", false), QuizOption(2, "गलत", true)),
            explanation = "यह क्लोरोप्लास्ट में होता है; माइटोकॉन्ड्रिया श्वसन संभालते हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_13", domain = ScienceDomain.PHYSICS,
            text = "मैक्सवेल के समीकरणों ने प्रकाश की गति से चलने वाली विद्युत चुम्बकीय तरंगों की भविष्यवाणी की थी?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "रेडियो तरंगें, दृश्य प्रकाश और X-किरणें सभी विद्युत चुम्बकीय तरंगें हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_14", domain = ScienceDomain.CHEMISTRY,
            text = "ठोसों का निश्चित आकार होता है क्योंकि उनके कण निश्चित स्थितियों के आसपास कंपन करते हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "तरलों और गैसों में कण बहुत अधिक स्वतंत्र रूप से चलते हैं।",
            difficulty = "INTERMEDIATE"
        ),
        ScienceQuestion(
            id = "r3_15", domain = ScienceDomain.BIOLOGY,
            text = "प्रतिवर्ती क्रियाएँ तीव्र, स्वचालित प्रतिक्रियाएँ हैं जो अक्सर सचेत विचार से पहले होती हैं?",
            options = listOf(QuizOption(1, "सही", true), QuizOption(2, "गलत", false)),
            explanation = "मस्तिष्क को पता चलने से पहले ही मेरुदंड कई प्रतिवर्तों को संभाल लेता है।",
            difficulty = "INTERMEDIATE"
        )
    )
}
