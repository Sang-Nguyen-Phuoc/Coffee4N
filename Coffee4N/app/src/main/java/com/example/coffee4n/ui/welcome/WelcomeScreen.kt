package com.example.coffee4n.ui.welcome

import android.app.Application
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.coffee4n.R
import com.example.coffee4n.model.Owner
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.session.OwnerSession
import com.example.coffee4n.ui.components.LanguageSelector
import com.example.coffee4n.ui.owner_inventory.ingredient_tab.IngredientTabViewModel

@Composable
fun WelcomeScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val viewModel: WelcomeScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WelcomeScreenViewModel(context.applicationContext as Application) as T
            }
        }
    )
    val owners by viewModel.owners.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F2ED))
            .padding(16.dp).padding(PaddingValues(top = 40.dp))

    ) {
        // Language selector at the top right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            LanguageSelector(
                onLanguageSelected = {
                    // Restart the activity to apply language changes
                    (context as? androidx.activity.ComponentActivity)?.recreate()
                }
            )
        }

        Row (
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp)
        ){
            Row (
                verticalAlignment = Alignment.CenterVertically,

                ) {
                Text(
                    text = stringResource(R.string.choose_store),
                    color = Color.Black,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = "Shop Icon",
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF5A9280)
                )
            } else {
                if (owners.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_stores_available),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(owners) { owner ->
                            Button(
                                onClick = {
                                    editor.putString("ownerId", owner.ownerId)
                                    editor.apply()

                                    OwnerSession.ownerId = owner.ownerId
                                    navController.navigate(Destinations.LOGIN)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.Unspecified
                                ),
                                elevation = null,
                                contentPadding = PaddingValues(0.dp),
                                shape = RectangleShape
                            ) {
                                OwnerCard(owner = owner)
                            }

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerCard(owner: Owner) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD8E2DC)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = owner.avatarUrl,
                contentDescription = "${owner.shopName} avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = owner.shopName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    ),
                    color = Color(0xFF5A9280)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = owner.shopAddress,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OwnerCardPreview() {
    OwnerCard(
        owner = Owner(
            ownerId = "1",
            email = "owner1@example.com",
            avatarUrl = "https://d2e5ushqwiltxm.cloudfront.net/wp-content/uploads/sites/48/2024/08/16031419/Ikigai-Garden-Cafe.png",
            passCode = "1234",
            shopName = "Coffee Haven",
            shopAddress = "123 Main St, City"
        )
    )
}