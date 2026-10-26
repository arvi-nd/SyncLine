package com.example.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.GlobeWireframe
import com.example.components.WisePillButton
import com.example.ui.theme.NearBlack
import com.example.ui.theme.WiseLime

@Composable
fun SplashScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(600))
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = WiseLime
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .alpha(alphaAnim.value),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Top Section: Headline and Subtitle
            Column(modifier = Modifier.padding(top = 28.dp)) {
                Text(
                    text = "Work\nbeyond\nborders.",
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 46.sp,
                    letterSpacing = (-1.5).sp,
                    color = NearBlack
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Chat. Collaborate. Get paid.\nWith clients worldwide.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp,
                    color = NearBlack.copy(alpha = 0.85f)
                )
            }

            // Center: Minimal Globe Wireframe Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlobeWireframe(
                    modifier = Modifier.size(220.dp),
                    lineColor = NearBlack
                )
            }

            // Bottom Call-to-Action
            Surface(
                onClick = onGetStarted,
                shape = RoundedCornerShape(9999.dp),
                color = NearBlack,
                contentColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("splash_get_started_button")
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Get Started",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
