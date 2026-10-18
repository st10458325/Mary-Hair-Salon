package za.co.maryhairsalon.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import za.co.maryhairsalon.ui.components.AppTextField
import za.co.maryhairsalon.ui.components.BrandMark
import za.co.maryhairsalon.ui.components.GhostButton
import za.co.maryhairsalon.ui.components.GlassCard
import za.co.maryhairsalon.ui.components.GradientButton
import za.co.maryhairsalon.ui.components.PasswordField
import za.co.maryhairsalon.ui.theme.Brand

/* Login / Sign up / Forgot password — same layout as the website's auth pages. */

@Composable
private fun AuthScaffold(title: String, subtitle: String, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brand.Bg)) {
        // Soft orange glow behind the panel (website: .glow-field)
        Box(
            Modifier
                .size(360.dp)
                .offset(x = (-120).dp, y = (-80).dp)
                .background(Brand.GlowGradient, RoundedCornerShape(50)),
        )
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            BrandMark(large = true)
            Spacer(Modifier.height(28.dp))
            GlassCard(Modifier.fillMaxWidth(), contentPadding = 20.dp) {
                Text(title, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = Brand.TextDim)
                Spacer(Modifier.height(20.dp))
                content()
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MessageBox(error: String?, info: String?) {
    val text = error ?: info ?: return
    val colour = if (error != null) Brand.Danger else Brand.Success
    Text(
        text,
        color = colour,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colour.copy(alpha = 0.12f))
            .padding(12.dp),
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun GoogleButton(enabled: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 14.dp)) {
        HorizontalDivider(Modifier.weight(1f), color = Brand.GlassBorder)
        Text("  or  ", color = Brand.TextFaint, style = MaterialTheme.typography.bodySmall)
        HorizontalDivider(Modifier.weight(1f), color = Brand.GlassBorder)
    }
    GhostButton(
        text = "Continue with Google",
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        leading = {
            // Multi-coloured "G" (keeps the app free of extra image assets)
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 18.sp)) { append("G") }
                },
            )
        },
    )
}

@Composable
fun LoginScreen(vm: AuthViewModel, onSignUp: () -> Unit, onForgot: () -> Unit) {
    val s by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.clearMessages() }

    AuthScaffold("Welcome back", "Log in to book and manage your appointments.") {
        AppTextField(s.email, vm::onEmail, "Email", keyboardType = KeyboardType.Email, leadingIcon = Icons.Default.Email, error = s.fieldErrors["email"])
        Spacer(Modifier.height(10.dp))
        PasswordField(s.password, vm::onPassword, error = s.fieldErrors["password"])
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onForgot) { Text("Forgot password?", color = Brand.OrangeLight) }
        }
        MessageBox(s.error, s.info)
        GradientButton("Log In", vm::login, Modifier.fillMaxWidth(), loading = s.loading)
        GoogleButton(enabled = !s.loading) { vm.signInWithGoogle(context) }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("New to Mary Hair Salon?", color = Brand.TextDim)
            TextButton(onClick = onSignUp) { Text("Create an account", color = Brand.OrangeLight) }
        }
    }
}

@Composable
fun SignUpScreen(vm: AuthViewModel, onLogin: () -> Unit) {
    val s by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.clearMessages() }

    AuthScaffold("Create your account", "Book appointments and track them in one place.") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppTextField(s.name, vm::onName, "Name", Modifier.weight(1f), error = s.fieldErrors["name"])
            AppTextField(s.surname, vm::onSurname, "Surname", Modifier.weight(1f), error = s.fieldErrors["surname"])
        }
        Spacer(Modifier.height(10.dp))
        AppTextField(s.email, vm::onEmail, "Email", keyboardType = KeyboardType.Email, leadingIcon = Icons.Default.Email, error = s.fieldErrors["email"])
        Spacer(Modifier.height(10.dp))
        AppTextField(s.contact, vm::onContact, "Contact (Cell No. / WhatsApp)", keyboardType = KeyboardType.Phone, leadingIcon = Icons.Default.Phone, placeholder = "e.g. 081 234 5678", error = s.fieldErrors["contact"])
        Spacer(Modifier.height(10.dp))
        PasswordField(s.password, vm::onPassword, error = s.fieldErrors["password"])
        Spacer(Modifier.height(10.dp))
        PasswordField(s.confirmPassword, vm::onConfirm, label = "Confirm Password", error = s.fieldErrors["confirm"])
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
            Checkbox(
                checked = s.acceptedTerms,
                onCheckedChange = vm::onTerms,
                colors = CheckboxDefaults.colors(checkedColor = Brand.Orange),
            )
            Text("I agree to the Terms of Service and Privacy Policy", color = Brand.TextDim, style = MaterialTheme.typography.bodySmall)
        }
        MessageBox(s.error, s.info)
        GradientButton("Create Account", vm::signUp, Modifier.fillMaxWidth(), loading = s.loading)
        GoogleButton(enabled = !s.loading) { vm.signInWithGoogle(context) }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account?", color = Brand.TextDim)
            TextButton(onClick = onLogin) { Text("Log in", color = Brand.OrangeLight) }
        }
    }
}

@Composable
fun ForgotPasswordScreen(vm: AuthViewModel, onBack: () -> Unit) {
    val s by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.clearMessages() }

    AuthScaffold("Reset your password", "Enter your email and we'll send you a link to reset it.") {
        AppTextField(s.email, vm::onEmail, "Email", keyboardType = KeyboardType.Email, leadingIcon = Icons.Default.Email, error = s.fieldErrors["email"])
        Spacer(Modifier.height(14.dp))
        MessageBox(s.error, s.info)
        GradientButton("Send Reset Link", vm::sendReset, Modifier.fillMaxWidth(), loading = s.loading)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Remembered it?", color = Brand.TextDim)
            TextButton(onClick = onBack) { Text("Back to Log In", color = Brand.OrangeLight) }
        }
    }
}
