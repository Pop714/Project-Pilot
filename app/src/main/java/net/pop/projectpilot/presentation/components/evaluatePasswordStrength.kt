package net.pop.projectpilot.presentation.components

fun evaluatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.NONE

    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
        password.length < 6 -> PasswordStrength.WEAK
        score <= 1 -> PasswordStrength.WEAK
        score == 2 -> PasswordStrength.FAIR
        score == 3 -> PasswordStrength.GOOD
        else -> PasswordStrength.STRONG
    }
}