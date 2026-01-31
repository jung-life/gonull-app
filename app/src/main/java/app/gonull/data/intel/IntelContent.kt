package app.gonull.data.intel

object IntelContent {
    val allIntel = listOf(
        IntelItem(
            id = 1,
            title = "The 30-Second Rule",
            body = "Impulses originate in the Amygdala. Logic lives in the Prefrontal Cortex. The logic center needs about 30 seconds to override the impulse. We don't block you to be mean; we block you to buy your brain time to think.",
            category = "Neuroscience"
        ),
        IntelItem(
            id = 2,
            title = "The Withdrawal Fog",
            body = "Feeling irritable or bored? Good. That's the feeling of your dopamine receptors recalibrating. If you can sit with this boredom for 10 minutes without scrolling, you are actively increasing your neuroplasticity.",
            category = "Neuroscience"
        ),
        IntelItem(
            id = 3,
            title = "Cumulative Repair",
            body = "Don't obsess over streaks. If you slip up, you haven't lost your progress. Neural pathways degrade over time, not overnight. Just get back on track immediately. Focus on your monthly Win Rate, not a fragile chain of days.",
            category = "Psychology"
        ),
        IntelItem(
            id = 4,
            title = "Intermittent Reinforcement",
            body = "Social media uses the same mechanic as slot machines: 'Variable Rewards.' You scroll because you *don't* know what you'll find. This uncertainty spikes dopamine 4x higher than predictable rewards. Predictability kills addiction.",
            category = "Psychology"
        ),
        IntelItem(
            id = 5,
            title = "Urge Surfing",
            body = "An urge typically lasts only 15-20 minutes. If you can 'surf' the wave of discomfort without acting on it, the urge will crash and fade. Every time you surf an urge, the next one becomes weaker.",
            category = "Strategy"
        )
    )

    fun getRandomIntel(): IntelItem {
        return allIntel.random()
    }

    fun getDailyIntel(dayOfYear: Int): IntelItem {
        // Deterministic random based on day of year so it stays consistent throughout the day
        val index = dayOfYear % allIntel.size
        return allIntel[index]
    }
}
