package com.example.coffee4n.service

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.generationConfig
import com.example.coffee4n.ui.insights.DailyRevenue
import com.example.coffee4n.ui.insights.HourlyData
import com.example.coffee4n.ui.insights.OrderItemStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(private val apiKey: String) {

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-pro",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.2f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 1000
            }
        )
    }

    /**
     * Generate insights about the overall business performance
     */
    suspend fun generateBusinessInsights(
        totalRevenue: Double,
        periodName: String,
        revenueData: List<DailyRevenue>,
        peakHoursData: List<HourlyData>,
        topProducts: List<OrderItemStat>
    ): String = withContext(Dispatchers.IO) {
        try {
            val prompt = buildString {
                append("As a business intelligence assistant for a coffee shop, analyze the following data and provide 3-4 concise, actionable insights:")
                append("\n\nTotal Revenue for $periodName: $${String.format("%.2f", totalRevenue)}")

                append("\n\nRevenue by Day:")
                revenueData.forEach {
                    append("\n- ${it.dayName}: $${String.format("%.2f", it.amount)}")
                }

                append("\n\nPeak Business Hours:")
                peakHoursData.forEach {
                    append("\n- ${it.hourRange}: ${it.orderCount} orders")
                }

                append("\n\nTop Selling Products:")
                topProducts.take(5).forEach {
                    append("\n- ${it.productName}: ${it.count} orders")
                }

                append("\n\nProvide short, specific insights about revenue patterns, peak hours optimization, and product performance. Format each insight as a bullet point starting with '•'. Keep the entire response under 350 characters.")
            }

            val response = model.generateContent(prompt)
            return@withContext response.text?.trim() ?: "Unable to generate insights at this time."
        } catch (e: Exception) {
            return@withContext "Could not generate insights: ${e.message}"
        }
    }

    /**
     * Generate specific product recommendations based on current sales data
     */
    suspend fun generateProductRecommendations(
        topProducts: List<OrderItemStat>
    ): String = withContext(Dispatchers.IO) {
        try {
            val prompt = buildString {
                append("As a coffee shop analytics advisor, review our top selling products:")

                topProducts.take(8).forEach {
                    append("\n- ${it.productName}: ${it.count} orders")
                }

                append("\n\nBased on these sales patterns, suggest 2-3 specific product recommendations or promotions that could boost sales. Format each recommendation as a numbered point. Keep the entire response under 300 characters.")
            }

            val response = model.generateContent(prompt)
            return@withContext response.text?.trim() ?: "Unable to generate product recommendations at this time."
        } catch (e: Exception) {
            return@withContext "Could not generate recommendations: ${e.message}"
        }
    }

    /**
     * Generate revenue trend analysis
     */
    suspend fun analyzeRevenueTrend(
        revenueData: List<DailyRevenue>
    ): String = withContext(Dispatchers.IO) {
        try {
            // Calculate some basic statistics
            val totalRevenue = revenueData.sumOf { it.amount }
            val averageRevenue = if (revenueData.isNotEmpty()) totalRevenue / revenueData.size else 0.0
            val maxRevenue = revenueData.maxOfOrNull { it.amount } ?: 0.0
            val maxRevenueDay = revenueData.maxByOrNull { it.amount }?.dayName ?: "None"
            val minRevenue = revenueData.minOfOrNull { it.amount } ?: 0.0
            val minRevenueDay = revenueData.minByOrNull { it.amount }?.dayName ?: "None"

            val prompt = buildString {
                append("As a revenue analyst for a coffee shop, analyze the following daily revenue data:")

                revenueData.forEach {
                    append("\n- ${it.dayName}: $${String.format("%.2f", it.amount)}")
                }

                append("\n\nBasic statistics:")
                append("\n- Total revenue: $${String.format("%.2f", totalRevenue)}")
                append("\n- Average daily revenue: $${String.format("%.2f", averageRevenue)}")
                append("\n- Highest revenue: $${String.format("%.2f", maxRevenue)} on $maxRevenueDay")
                append("\n- Lowest revenue: $${String.format("%.2f", minRevenue)} on $minRevenueDay")

                append("\n\nProvide a very concise trend analysis in 1-2 sentences highlighting the main revenue pattern. Keep the response under 200 characters.")
            }

            val response = model.generateContent(prompt)
            return@withContext response.text?.trim() ?: "Unable to analyze revenue trends at this time."
        } catch (e: Exception) {
            return@withContext "Could not analyze trends: ${e.message}"
        }
    }
}