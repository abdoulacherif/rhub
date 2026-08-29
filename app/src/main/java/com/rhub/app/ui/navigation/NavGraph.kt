package com.rhub.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rhub.app.ui.auth.LoginScreen
import com.rhub.app.ui.director.DirectorHomeScreen
import com.rhub.app.ui.employee.EmployeeHomeScreen

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val DIRECTOR_HOME = "director_home"
    const val EMPLOYEE_HOME = "employee_home"
}

@Composable
fun RHubNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onConnecte = { role ->
                    val destination = if (role == "directeur" || role == "super_admin")
                        Routes.DIRECTOR_HOME else Routes.EMPLOYEE_HOME
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onAllerInscription = { navController.navigate(Routes.SIGNUP) }
            )
        }

        composable(Routes.SIGNUP) {
            // SignupScreen à construire sur le même modèle que LoginScreen
        }

        composable(Routes.DIRECTOR_HOME) { DirectorHomeScreen() }
        composable(Routes.EMPLOYEE_HOME) { EmployeeHomeScreen() }
    }
}