package com.example.inscit.xp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class StreakCheckWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        StreakTracker.checkAndResetIfMissed(applicationContext)
        return Result.success()
    }
}
