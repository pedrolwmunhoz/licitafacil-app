package com.licitafacil.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.licitafacil.app.data.local.LocalAuth
import com.licitafacil.app.data.local.SessionStore
import com.licitafacil.app.data.log.AppLogger
import com.licitafacil.app.data.repository.AppRepository
import com.licitafacil.app.ui.config.ConfigScreen
import com.licitafacil.app.ui.conta.ContaScreen
import com.licitafacil.app.ui.detalhe.DetalheScreen
import com.licitafacil.app.ui.lista.ListaScreen
import com.licitafacil.app.ui.login.LoginScreen
import com.licitafacil.app.ui.shell.AppViewModel
import com.licitafacil.app.ui.shell.AppViewModelFactory
import com.licitafacil.app.ui.theme.Ink
import com.licitafacil.app.ui.util.configSummary
import com.licitafacil.app.ui.util.initialsOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object Routes {
    const val Boot = "boot"
    const val Login = "login"
    const val Config = "config"
    const val Lista = "lista"
    const val Conta = "conta"
    const val Detalhe = "detalhe/{cnpj}/{ano}/{seq}"
    fun detalhe(cnpj: String, ano: Int, seq: Int) = "detalhe/$cnpj/$ano/$seq"
}

@Composable
fun LicitaFacilNav() {
    val context = LocalContext.current.applicationContext
    val repo = remember {
        LocalAuth.ensureFile(context)
        AppRepository(SessionStore(context), context)
    }
    val vm: AppViewModel = viewModel(factory = AppViewModelFactory(repo))
    val nav = rememberNavController()
    val session by vm.session.collectAsState()
    val config by vm.config.collectAsState()
    val scope = rememberCoroutineScope()
    var hadSession by remember { mutableStateOf(false) }

    LaunchedEffect(session) {
        if (session != null) hadSession = true
        else if (hadSession) {
            // Sessão limpa (logout manual ou 401) → login
            nav.navigate(Routes.Login) { popUpTo(0) { inclusive = true } }
            hadSession = false
        }
    }

    NavHost(navController = nav, startDestination = Routes.Boot) {
        composable(Routes.Boot) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Ink)
            }
            LaunchedEffect(Unit) {
                val s = repo.session.first()
                val c = repo.config.first()
                AppLogger.setUsuarioId(s?.usuarioId)
                val dest = when {
                    s == null -> Routes.Login
                    c.keywords.isEmpty() -> Routes.Config
                    else -> Routes.Lista
                }
                nav.navigate(dest) { popUpTo(Routes.Boot) { inclusive = true } }
            }
        }
        composable(Routes.Login) {
            LoginScreen(
                loading = vm.loading,
                error = vm.error,
                serverErrorPopup = vm.serverErrorPopup,
                onDismissServerError = { vm.dismissServerError() },
                onLogin = { email, senha ->
                    scope.launch {
                        val ok = vm.login(email, senha)
                        if (ok) {
                            if (vm.config.value.keywords.isEmpty()) {
                                nav.navigate(Routes.Config) { popUpTo(Routes.Login) { inclusive = true } }
                            } else {
                                nav.navigate(Routes.Lista) { popUpTo(Routes.Login) { inclusive = true } }
                            }
                        }
                    }
                },
            )
        }
        composable(Routes.Config) {
            ConfigScreen(
                initial = config,
                onSave = { c ->
                    scope.launch {
                        vm.saveConfig(c)
                        nav.navigate(Routes.Lista) {
                            popUpTo(Routes.Config) { inclusive = true }
                        }
                    }
                },
                onCancel = if (config.keywords.isNotEmpty()) ({ nav.popBackStack() }) else null,
                onLogout = {
                    scope.launch {
                        vm.logout()
                        nav.navigate(Routes.Login) { popUpTo(0) { inclusive = true } }
                    }
                },
            )
        }
        composable(Routes.Lista) {
            ListaScreen(
                editais = vm.editais,
                loading = vm.loading,
                loadingMore = vm.loadingMore,
                hasMore = vm.hasMore,
                totalAbertos = vm.totalAbertos,
                error = vm.error,
                offline = vm.offline,
                configSummary = configSummary(config),
                initials = initialsOf(session?.nome),
                onRefresh = { scope.launch { vm.carregarEditais() } },
                onLoadMore = { scope.launch { vm.carregarMaisEditais() } },
                onOpen = { item ->
                    val c = item.cnpj()
                    val a = item.anoCompra
                    val s = item.sequencialCompra
                    if (c.isNotBlank() && a != null && s != null) {
                        nav.navigate(Routes.detalhe(c, a, s))
                    }
                },
                onConfig = { nav.navigate(Routes.Config) },
                onConta = { nav.navigate(Routes.Conta) },
            )
        }
        composable(
            Routes.Detalhe,
            arguments = listOf(
                navArgument("cnpj") { type = NavType.StringType },
                navArgument("ano") { type = NavType.IntType },
                navArgument("seq") { type = NavType.IntType },
            ),
        ) { entry ->
            val cnpj = entry.arguments?.getString("cnpj").orEmpty()
            val ano = entry.arguments?.getInt("ano") ?: 0
            val seq = entry.arguments?.getInt("seq") ?: 0
            DetalheScreen(
                loading = vm.loading,
                error = vm.error,
                offline = vm.offline,
                item = vm.detalhe,
                onLoad = { scope.launch { vm.carregarDetalhe(cnpj, ano, seq) } },
                onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.Conta) {
            ContaScreen(
                session = session,
                config = config,
                onConfig = { nav.navigate(Routes.Config) },
                onLogout = {
                    scope.launch {
                        vm.logout()
                        nav.navigate(Routes.Login) { popUpTo(0) { inclusive = true } }
                    }
                },
                onBack = { nav.popBackStack() },
            )
        }
    }
}
