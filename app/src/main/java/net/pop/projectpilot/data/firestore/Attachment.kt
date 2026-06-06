package net.pop.projectpilot.data.firestore

data class Attachment(
    val id: String = "",
    val taskId: String = "",
    val title: String = "",
    val type: String = "",
    val file: String = "",
    val url: String = "",
    val createdAt: String = ""
)
