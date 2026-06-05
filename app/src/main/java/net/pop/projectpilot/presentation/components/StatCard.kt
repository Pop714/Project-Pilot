package net.pop.projectpilot.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    containerColor: Color,
    contentColor: Color,
    imageVectorSrc: ImageVector,
    isHorizontal: Boolean = false
) {
    Card(
        modifier = modifier.height(150.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (isHorizontal) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(contentColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SocialDistance,
                        tint = contentColor,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = number,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(text = label, fontSize = 12.sp, color = contentColor.copy(alpha = 0.8f))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(contentColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = imageVectorSrc,
                        tint = contentColor,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = number,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}