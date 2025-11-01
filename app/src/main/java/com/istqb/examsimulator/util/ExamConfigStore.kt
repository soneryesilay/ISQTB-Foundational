package com.istqb.examsimulator.util

import com.istqb.examsimulator.data.model.ExamConfig

object ExamConfigStore {
    private var configMap = mutableMapOf<String, ExamConfig>()

    fun saveConfig(attemptId: String, config: ExamConfig) {
        configMap[attemptId] = config
    }

    fun getConfig(attemptId: String): ExamConfig? {
        return configMap[attemptId]
    }

    fun removeConfig(attemptId: String) {
        configMap.remove(attemptId)
    }
}

