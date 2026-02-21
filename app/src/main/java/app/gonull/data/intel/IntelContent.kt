package app.gonull.data.intel

object IntelContent {
    val allIntel = listOf(
        // --- Neuroscience ---
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
            id = 6,
            title = "Dopamine Baseline",
            body = "Every scroll-session temporarily raises dopamine, then crashes it below your baseline. Over weeks of heavy use, your baseline drops permanently. Abstaining lets it recover. The first 2 weeks feel the worst because you're below baseline.",
            category = "Neuroscience"
        ),
        IntelItem(
            id = 7,
            title = "Default Mode Network",
            body = "When you're bored and not reaching for your phone, your brain activates the Default Mode Network. This is where creativity, self-reflection, and future planning happen. Constant scrolling suppresses this network.",
            category = "Neuroscience"
        ),
        IntelItem(
            id = 8,
            title = "Attention Residue",
            body = "After checking social media, your brain keeps processing it for 15-23 minutes. This 'attention residue' makes everything else feel harder. A quick 2-minute check actually costs 20 minutes of focus.",
            category = "Neuroscience"
        ),
        IntelItem(
            id = 9,
            title = "Cortisol & Comparison",
            body = "Scrolling through curated highlight reels triggers cortisol, the stress hormone. Your brain treats social comparison as a survival threat. Less scrolling measurably lowers cortisol within one week.",
            category = "Neuroscience"
        ),
        IntelItem(
            id = 10,
            title = "The Novelty Bias",
            body = "Your brain is hardwired to prioritize new information over important information. Every notification hijacks this ancient survival mechanism. That red badge isn't urgent; it's just new.",
            category = "Neuroscience"
        ),
        IntelItem(
            id = 11,
            title = "Prefrontal Fatigue",
            body = "Your willpower lives in the prefrontal cortex, and it's a limited resource. Every 'should I check my phone?' decision drains it. Removing the option entirely with a blocker preserves willpower for things that matter.",
            category = "Neuroscience"
        ),

        // --- Psychology ---
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
            id = 12,
            title = "The Zeigarnik Effect",
            body = "Your brain obsesses over incomplete tasks. Social media exploits this with infinite feeds that never 'finish.' When you close the app, you feel pulled back because your brain wants closure that will never come.",
            category = "Psychology"
        ),
        IntelItem(
            id = 13,
            title = "Identity-Based Habits",
            body = "Instead of 'I'm trying to use my phone less,' try 'I'm someone who doesn't mindlessly scroll.' Research shows framing change as identity rather than behavior makes it 2-3x more likely to stick.",
            category = "Psychology"
        ),
        IntelItem(
            id = 14,
            title = "The Paradox of Choice",
            body = "Having fewer options makes you happier with what you choose. By blocking apps, you're not losing freedom, you're gaining satisfaction. People with constraints report higher well-being than those with unlimited access.",
            category = "Psychology"
        ),
        IntelItem(
            id = 15,
            title = "Hedonic Adaptation",
            body = "Your brain adapts to any pleasure until it feels normal. The first scroll felt amazing. Now you need hours to feel the same hit. This is tolerance, and it works identically to substance addiction.",
            category = "Psychology"
        ),
        IntelItem(
            id = 16,
            title = "Present Bias",
            body = "Humans overvalue immediate rewards and undervalue future ones. Your present self wants to scroll. Your future self wants the time back. The delay timer works because it forces your present self to wait for your future self to arrive.",
            category = "Psychology"
        ),
        IntelItem(
            id = 17,
            title = "Social Proof Trap",
            body = "You think everyone is on social media all the time because everyone posts about being on social media. In reality, the heaviest users are the most visible. Quiet people living full lives don't post about it.",
            category = "Psychology"
        ),

        // --- Strategy ---
        IntelItem(
            id = 5,
            title = "Urge Surfing",
            body = "An urge typically lasts only 15-20 minutes. If you can 'surf' the wave of discomfort without acting on it, the urge will crash and fade. Every time you surf an urge, the next one becomes weaker.",
            category = "Strategy"
        ),
        IntelItem(
            id = 18,
            title = "Environment Design",
            body = "Willpower is unreliable. Instead, redesign your environment. Charge your phone in another room. Delete app icons. Make friction your ally. The best strategy isn't resisting temptation; it's never encountering it.",
            category = "Strategy"
        ),
        IntelItem(
            id = 19,
            title = "The 2-Minute Replacement",
            body = "When you feel the urge to scroll, do something physical for 2 minutes instead: push-ups, stretch, walk to a window. Physical movement releases enough dopamine to satisfy the craving without the screen.",
            category = "Strategy"
        ),
        IntelItem(
            id = 20,
            title = "Batching Over Checking",
            body = "Instead of checking messages throughout the day, batch them into 2-3 scheduled windows. You'll respond to the same messages but spend 60% less total time. Urgent things find you through calls.",
            category = "Strategy"
        ),
        IntelItem(
            id = 21,
            title = "The Boredom Protocol",
            body = "Boredom is not a problem to solve. It's a signal that your brain is ready to create, plan, or rest. The next time you feel bored, resist the phone for 5 minutes. Notice what thoughts and ideas emerge.",
            category = "Strategy"
        ),
        IntelItem(
            id = 22,
            title = "Accountability Power",
            body = "Telling one person about your goal makes you 65% more likely to achieve it. Having regular check-ins pushes it to 95%. Share your GoNull stats with someone you respect.",
            category = "Strategy"
        ),

        // --- Research ---
        IntelItem(
            id = 23,
            title = "The Screen Time Studies",
            body = "Reducing social media to 30 minutes per day for 3 weeks significantly decreased loneliness and depression in a University of Pennsylvania study. The effect was strongest in people who started with the highest usage.",
            category = "Research"
        ),
        IntelItem(
            id = 24,
            title = "Deep Work Math",
            body = "Cal Newport found that knowledge workers average only 2.5 hours of deep focus per day. Phone interruptions are the #1 cause. Eliminating them can double your meaningful output without working longer hours.",
            category = "Research"
        ),
        IntelItem(
            id = 25,
            title = "Sleep & Blue Light",
            body = "Using your phone within 1 hour of bedtime delays melatonin release by 90 minutes and reduces REM sleep by 20%. Poor sleep then makes you more impulsive the next day, creating a vicious cycle.",
            category = "Research"
        ),
        IntelItem(
            id = 26,
            title = "The Mere Presence Effect",
            body = "A University of Texas study found that having your phone visible on the desk, even face down and silent, reduces cognitive capacity by 10%. Your brain spends energy resisting the urge to check it.",
            category = "Research"
        ),
        IntelItem(
            id = 27,
            title = "Reclaiming 2 Hours",
            body = "The average person spends 2 hours 27 minutes daily on social media. That's 37 full days per year. Cutting it in half gives you back 18 days annually. What could you build with 18 extra days?",
            category = "Research"
        ),

        // --- Mindset ---
        IntelItem(
            id = 28,
            title = "You're Not Missing Out",
            body = "FOMO is manufactured. Social media companies engineer the feeling that something important is happening without you. In reality, 99% of what you'd see is content you won't remember tomorrow.",
            category = "Mindset"
        ),
        IntelItem(
            id = 29,
            title = "Discomfort Is Progress",
            body = "If blocking apps feels uncomfortable, it's working. Comfort with constant stimulation is the problem. The discomfort you feel right now is the same discomfort that builds every meaningful skill.",
            category = "Mindset"
        ),
        IntelItem(
            id = 30,
            title = "The Craftsman Mindset",
            body = "Ask: 'Does this app add concrete value to my life, or does it just fill time?' If an app's main benefit is killing boredom, it's not a tool. It's a pacifier. Adults don't need pacifiers.",
            category = "Mindset"
        ),
        IntelItem(
            id = 31,
            title = "Progress, Not Perfection",
            body = "You don't need to quit cold turkey. Reducing from 4 hours to 2 hours of daily use is a massive win. Your dopamine system doesn't need zero stimulation. It needs balance. Aim for intentional use, not zero use.",
            category = "Mindset"
        ),
        IntelItem(
            id = 32,
            title = "Reclaim Your Mornings",
            body = "The first 30 minutes of your day set the tone for everything. If you start by scrolling, your brain enters reactive mode. If you start with intention, planning, or movement, your brain enters creative mode.",
            category = "Mindset"
        ),
        IntelItem(
            id = 33,
            title = "Delayed Gratification Compounds",
            body = "Every time you resist an impulse, you strengthen your ability to resist the next one. Self-control is a muscle. The people you admire aren't built differently. They've just trained this muscle longer.",
            category = "Mindset"
        )
    )

    fun getRandomIntel(): IntelItem {
        return allIntel.random()
    }

    fun getDailyIntel(dayOfYear: Int): IntelItem {
        // Deterministic selection based on day of year, consistent throughout the day
        // Use a prime multiplier to avoid sequential cycling through the list
        val index = (dayOfYear * 7) % allIntel.size
        return allIntel[index]
    }
}
