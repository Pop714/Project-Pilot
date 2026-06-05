package net.pop.projectpilot.data.firestore

data class Task(
    val id: String = "",
    val title: String = "",
    val status: String = "",
    val createdAt: String = "",
    val voicePath: String = ""
)
