package snake.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import snake.theme.ElectricYellow
import snake.theme.TextPrimary
import snake.theme.TextSecondary
import snake.theme.neonBorder

@Composable
fun GameOverScreen(score: Int, highScore: Int, onRetryClicked: () -> Unit) {
    val text = if (score == highScore) {
        "NOVO RECORDE: $score"
    } else {
        "ENERGIA: $score"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .neonBorder()
            .padding(18.dp)
    ) {
        Text(
            text = "FLUXO INTERROMPIDO",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text, fontSize = 16.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetryClicked,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricYellow.copy(alpha = 0.16f))
        ) {
            Text(
                "REINICIAR",
                color = ElectricYellow,
                fontWeight = FontWeight.Bold
            )
        }
    }
}