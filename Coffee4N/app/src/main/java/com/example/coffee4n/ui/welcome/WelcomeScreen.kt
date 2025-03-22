package com.example.coffee4n.ui.welcome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.coffee4n.navigation.Destinations

@Composable
fun WelcomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Column để sắp xếp các phần tử theo chiều dọc
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            // Nút "Login"
            Button(
                onClick = { navController.navigate(Destinations.LOGIN) },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E2E2E), // Nền xám đậm
                    contentColor = Color.White // Chữ trắng
                ),
                shape = RoundedCornerShape(5.dp) // Góc bo tròn 5dp
            ) {
                Text("Login", fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 12.dp))
            }

            // Khoảng cách 20dp giữa "Login" và "Register"
            Spacer(modifier = Modifier.height(20.dp))

            // Nút "Register"
            Button(
                onClick = { navController.navigate(Destinations.SIGNUP) },
                modifier = Modifier
                    .fillMaxWidth() // Rộng 300dp
                    .wrapContentHeight(), // Cao 50dp
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, // Nền trắng
                    contentColor = Color.Black // Chữ đen
                ),
                shape = RoundedCornerShape(5.dp), // Góc bo tròn 5dp
                border = BorderStroke(1.dp, Color.Black) // Viền đen 1dp
            ) {
                Text("Register", fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp))
            }

            // Khoảng cách 30dp giữa "Register" và liên kết
            Spacer(modifier = Modifier.height(120.dp))

            // Liên kết "Continue as guest"
            TextButton(onClick = { navController.navigate(Destinations.HOME) }) {
                Text(
                    "Continue as a guest",
                    color = MaterialTheme.colorScheme.tertiary, // Màu xanh ngọc nhạt
                    fontSize = 14.sp, // Kích thước chữ 14sp
                    style = TextStyle(textDecoration = TextDecoration.Underline) // Gạch chân
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
