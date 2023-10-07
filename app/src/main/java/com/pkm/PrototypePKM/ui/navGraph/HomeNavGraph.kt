package com.pkm.PrototypePKM.ui.navGraph


import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pkm.PrototypePKM.BottomBarItem
import com.pkm.PrototypePKM.ui.screen.beranda.BerandaScreen
import com.pkm.PrototypePKM.ui.screen.feedback.FeedbackScreen
import com.pkm.PrototypePKM.ui.screen.prediksi.PrediksiScreen

@Composable
fun HomeNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        route = "graph_home",
        startDestination = BottomBarItem.Beranda.route,
    ){
        composable(route=BottomBarItem.Prediksi.route){
            PrediksiScreen()
        }
        composable(route=BottomBarItem.Beranda.route){
            BerandaScreen()
        }
        composable(route=BottomBarItem.Feedback.route){
            FeedbackScreen(navController)
        }
    }
}

