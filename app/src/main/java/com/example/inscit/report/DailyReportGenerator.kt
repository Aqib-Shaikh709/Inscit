package com.example.inscit.report

import com.example.inscit.models.UserDocument

object DailyReportGenerator {

    fun generateSentiment(userDoc: UserDocument): String {
        val xp = userDoc.stats.xp
        val quizzes = userDoc.stats.quizzesTaken
        
        return when {
            xp > 500 || quizzes > 10 -> "Wonderful effort today. Consistent learning activity and strong participation were observed."
            xp > 100 || quizzes > 3 -> "A balanced learning day was recorded with moderate activity."
            else -> "Today's activity was limited. Additional encouragement may help improve consistency."
        }
    }

    fun generateTextReport(userDoc: UserDocument): String {
        val metrics = UsageAnalyticsManager.collectMetrics(userDoc)
        val sentiment = generateSentiment(userDoc)
        val userName = userDoc.profile.name
        
        val metricsText = metrics.map { (k, v) -> "- $k: $v" }.joinToString("\n")

        return """
            📊 DAILY LEARNING REPORT CARD 📊
            
            Student: $userName
            Status: $sentiment
            
            📈 Performance Metrics:
            $metricsText
            
            Keep encouraging the learning journey!
            Generated via Inscit Omega
        """.trimIndent()
    }

    fun generateHtmlReport(userDoc: UserDocument): String {
        val metrics = UsageAnalyticsManager.collectMetrics(userDoc)
        val sentiment = generateSentiment(userDoc)
        val userName = userDoc.profile.name
        
        val tableRows = metrics.map { (k, v) ->
            "<tr><td style='padding: 8px; border: 1px solid #ddd;'>$k</td><td style='padding: 8px; border: 1px solid #ddd;'>$v</td></tr>"
        }.joinToString("")

        return """
            <html>
            <body style='font-family: sans-serif; color: #333;'>
                <h2>Daily Learning Report Card</h2>
                <p>Hello Parent,</p>
                <p>Here is the daily activity summary for your child, <b>$userName</b>:</p>
                <div style='background: #f9f9f9; padding: 15px; border-radius: 8px; border-left: 5px solid #00F2FF;'>
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
