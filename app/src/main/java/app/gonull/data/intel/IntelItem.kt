package app.gonull.data.intel

data class IntelItem(
    val id: Int,
    val title: String,
    val body: String,
    val category: String = "Neuroscience"
)
