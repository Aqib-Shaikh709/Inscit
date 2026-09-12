package com.example.inscit.report

import com.example.inscit.models.Lang
import com.example.inscit.models.UserDocument

object DailyReportGenerator {

    private const val REPORT_TITLE = "DAILY LEARNING REPORT CARD"
    private const val REPORT_FOOTER = "Keep encouraging the learning journey!"
    private const val REPORT_GENERATED_BY = "Generated via Inscit Omega"

    private fun escapeHtml(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

    fun generateSentiment(userDoc: UserDocument): String {
        val xp = userDoc.stats.xp
        val quizzes = userDoc.stats.quizzesTaken
        val isHindi = userDoc.settings.language == Lang.HI
        return when {
            xp > 500 || quizzes > 10 -> if (isHindi) "आज शानदार प्रयास रहा। लगातार सीखने और मजबूत भागीदारी देखी गई।" else "Wonderful effort today. Consistent learning activity and strong participation were observed."
            xp > 100 || quizzes > 3 -> if (isHindi) "आज संतुलित सीखने का दिन रहा, जिसमें मध्यम गतिविधि दर्ज की गई।" else "A balanced learning day was recorded with moderate activity."
            else -> if (isHindi) "आज की गतिविधि सीमित रही। निरंतरता सुधारने के लिए अतिरिक्त प्रोत्साहन मददगार हो सकता है।" else "Today's activity was limited. Additional encouragement may help improve consistency."
        }
    }

    fun generateTextReport(userDoc: UserDocument): String {
        val metrics = UsageAnalyticsManager.collectMetrics(userDoc)
        val sentiment = generateSentiment(userDoc)
        val userName = userDoc.profile.name
        
        val metricsText = metrics.map { (k, v) -> "- $k: $v" }.joinToString("\n")

        return """
            📊 $REPORT_TITLE 📊
            
            Student: $userName
            Status: $sentiment
            
            📈 Performance Metrics:
            $metricsText
            
            $REPORT_FOOTER
            $REPORT_GENERATED_BY
        """.trimIndent()
    }

    fun generateHtmlReport(userDoc: UserDocument, accentHex: String = "#00F2FF"): String {
        val metrics = UsageAnalyticsManager.collectMetrics(userDoc)
        val sentiment = generateSentiment(userDoc)
        val userName = escapeHtml(userDoc.profile.name)
        
        val tableRows = metrics.map { (k, v) ->
            "<tr><td style='padding: 8px; border: 1px solid #ddd;'>${escapeHtml(k)}</td><td style='padding: 8px; border: 1px solid #ddd;'>${escapeHtml(v)}</td></tr>"
        }.joinToString("")

        return """
            <html>
            <body style='font-family: sans-serif; color: #333;'>
                <h2>Daily Learning Report Card</h2>
                <p>Hello Parent,</p>
                <p>Here is the daily activity summary for your child, <b>$userName</b>:</p>
                <div style='background: #f9f9f9; padding: 15px; border-radius: 8px; border-left: 5px solid $accentHex;'>
                    <i>"$sentiment"</i>
                </div>
                <h3 style='margin-top: 20px;'>Performance Metrics</h3>
                <table style='width: 100%; border-collapse: collapse;'>
                    <thead>
                        <tr style='background: #eee;'>
                            <th style='padding: 8px; border: 1px solid #ddd; text-align: left;'>Metric</th>
                            <th style='padding: 8px; border: 1px solid #ddd; text-align: left;'>Value</th>
                        </tr>
                    </thead>
                    <tbody>
                        $tableRows
                    </tbody>
                </table>
                <p style='margin-top: 20px;'>Closing note: Keep encouraging the learning journey!</p>
                <p>Best Regards,<br>Inscit Omega Team</p>
            </body>
            </html>
        """.trimIndent()
    }
}
