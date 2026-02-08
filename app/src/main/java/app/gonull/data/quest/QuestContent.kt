package app.gonull.data.quest

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val category: QuestCategory,
    val durationMinutes: Int
)

enum class QuestCategory(val emoji: String, val label: String) {
    PHYSICAL("\uD83C\uDFCB\uFE0F", "Physical"),
    SOCIAL("\uD83D\uDC4B", "Social"),
    MINDFUL("\uD83E\uDDD8", "Mindful"),
    CREATIVE("\uD83C\uDFA8", "Creative")
}

object QuestContent {
    private val quests = listOf(
        Quest("pushups", "20 Pushups", "Drop and give yourself 20. Your body will thank you.", QuestCategory.PHYSICAL, 2),
        Quest("stretch", "Full Body Stretch", "Stretch your neck, shoulders, back, and legs for 3 minutes.", QuestCategory.PHYSICAL, 3),
        Quest("walk", "Walk Around the Block", "Step outside and walk around your block once.", QuestCategory.PHYSICAL, 5),
        Quest("jumping_jacks", "50 Jumping Jacks", "Get your heart rate up with 50 jumping jacks.", QuestCategory.PHYSICAL, 3),
        Quest("plank", "60-Second Plank", "Hold a plank for as long as you can, up to 60 seconds.", QuestCategory.PHYSICAL, 2),

        Quest("text_friend", "Text a Friend", "Send a genuine message to someone you haven't talked to recently.", QuestCategory.SOCIAL, 2),
        Quest("compliment", "Give a Compliment", "Find someone nearby and give them a genuine compliment.", QuestCategory.SOCIAL, 1),
        Quest("call_family", "Call a Family Member", "Call someone in your family just to say hi.", QuestCategory.SOCIAL, 5),
        Quest("ask_someone", "Ask Someone How They Are", "Have a real conversation. Ask and actually listen.", QuestCategory.SOCIAL, 3),
        Quest("thank_someone", "Thank Someone", "Think of someone who helped you recently and thank them.", QuestCategory.SOCIAL, 2),

        Quest("box_breathing", "Box Breathing", "4 seconds in, 4 hold, 4 out, 4 hold. Repeat 5 times.", QuestCategory.MINDFUL, 3),
        Quest("gratitude", "3 Things You're Grateful For", "Name three specific things you're grateful for right now.", QuestCategory.MINDFUL, 2),
        Quest("body_scan", "Quick Body Scan", "Close your eyes and slowly scan from head to toes, noticing sensations.", QuestCategory.MINDFUL, 3),
        Quest("window_gaze", "Look Out a Window", "Spend 2 minutes just looking outside. Notice what you see.", QuestCategory.MINDFUL, 2),
        Quest("drink_water", "Drink a Glass of Water", "Fill a glass, drink it slowly, and notice the sensation.", QuestCategory.MINDFUL, 2),

        Quest("haiku", "Write a Haiku", "5-7-5 syllables. Write about whatever you see around you.", QuestCategory.CREATIVE, 3),
        Quest("doodle", "Doodle Something", "Grab paper and draw whatever comes to mind for 2 minutes.", QuestCategory.CREATIVE, 2),
        Quest("observe", "Sketch Your Surroundings", "Draw a quick sketch of something you can see right now.", QuestCategory.CREATIVE, 5),
        Quest("story", "Write a 3-Sentence Story", "Beginning, middle, end. Any topic. Go.", QuestCategory.CREATIVE, 3),
        Quest("hum", "Hum a Melody", "Make up a melody and hum it. No phone music needed.", QuestCategory.CREATIVE, 2)
    )

    fun getRandomQuest(): Quest {
        return quests.random()
    }

    fun getQuestById(id: String): Quest? {
        return quests.find { it.id == id }
    }
}
