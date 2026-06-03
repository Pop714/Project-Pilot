package net.pop.projectpilot.presentation.components

import androidx.compose.ui.graphics.Color

enum class PasswordStrength(val label: String, val color: Color, val fraction: Float) {
    NONE("", Color.Transparent, 0f),
    WEAK("Weak - Add numbers or special characters", Color.Red, 0.25f),
    FAIR("Fair - Try adding uppercase letters", Color(0xFFFFA500), 0.5f),
    GOOD("Good - Almost there", Color(0xFFCDDC39), 0.75f),
    STRONG("Strong - Great password!", Color(0xFF4CAF50), 1f)
}