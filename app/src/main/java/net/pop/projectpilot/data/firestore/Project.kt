package net.pop.projectpilot.data.firestore

data class Project(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val priority: String = "",
    val status: String = "",
    val lastUpdated: String = "",
    val createdAt: String = "",
    val members: List<String> = emptyList(),
    val userId: String = ""
)
