package com.example.inscit.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.inscit.models.Lang
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class InactivityWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Toggle gate + hard 1/day cap (shared date key prevents double ping with goal reminders)
        if (!NotificationHelper.isEnabled(applicationContext)) return Result.success()
        val prefs = applicationContext.getSharedPreferences("notification_caps", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (prefs.getString("last_inactivity_date", "") == today) return Result.success()

        val lang = NotificationHelper.userLanguage(applicationContext)
        val (title, message) = getRandomMotivationalMessage(lang)
        NotificationHelper.showNotification(applicationContext, title, message)
        prefs.edit().putString("last_inactivity_date", today).apply()
        return Result.success()
    }

    private fun getRandomMotivationalMessage(lang: Lang): Pair<String, String> {
        val en = listOf(
            "Yo Explorer! 🚀" to "Your brain cells are literally begging for a quiz. Don't let them down!",
            "Brain Lag? 🧠" to "Is your IQ feeling lonely? Come back and give it some company with a quick science session!",
            "Newton is Crying 🍎" to "Gravity is still working, but are you? Let's drop some knowledge!",
            "Science called... 📞" to "It said it misses you. Don't be that person who ghosts Science. Not cool.",
            "Level Up? 🆙" to "Those XP points aren't going to earn themselves. Stop procrastinating!",
            "E = mc... what? ⚡" to "Einstein didn't invent physics for you to just scroll social media all day. Quiz time!",
            "Oxygen is Free 💨" to "But knowledge is priceless. Breathe in some science right now!",
            "Don't be a Neutron ⚛️" to "Stay positive, but also stay active! Your streak is waiting for you.",
            "Absolute Zero? 🧊" to "That's how much progress you're making right now. Let's heat things up!",
            "Main Character Energy ✨" to "The main character always knows their science. Don't be an NPC, take a quiz!"
        )
        val hi = listOf(
            "यो एक्सप्लोरर! 🚀" to "आपकी मस्तिष्क कोशिकाएं क्विज़ के लिए तरस रही हैं। उन्हें निराश न करें!",
            "न्यूटन रो रहे हैं 🍎" to "गुरुत्वाकर्षण अभी भी काम कर रहा है, पर क्या आप? चलो ज्ञान बरसाते हैं!",
            "विज्ञान ने बुलाया... 📞" to "वह आपको याद करता है। विज्ञान को नज़रअंदाज़ न करें!",
            "लेवल अप? 🆙" to "XP अंक खुद नहीं मिलेंगे। टालमटोल बंद करें!",
            "ऑक्सीजन मुफ्त है 💨" to "पर ज्ञान अनमोल है। अभी थोड़ा विज्ञान लें!",
            "न्यूट्रॉन न बनें ⚛️" to "सकारात्मक रहें और सक्रिय भी! आपकी स्ट्रीक आपका इंतज़ार कर रही है।",
            "मुख्य किरदार एनर्जी ✨" to "मुख्य किरदार हमेशा विज्ञान जानता है। NPC न बनें, क्विज़ लें!"
        )
        val pool = if (lang == Lang.HI) hi else en
        return pool[Random.nextInt(pool.size)]
    }
}
