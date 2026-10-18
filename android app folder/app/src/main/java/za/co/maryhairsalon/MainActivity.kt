package za.co.maryhairsalon

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import za.co.maryhairsalon.data.remote.SupabaseProvider
import za.co.maryhairsalon.ui.AppRoot
import za.co.maryhairsalon.ui.components.BrandMark
import za.co.maryhairsalon.ui.theme.Brand
import za.co.maryhairsalon.ui.theme.MaryHairSalonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Without SUPABASE_URL / SUPABASE_ANON_KEY in local.properties the app can't reach the
        // database. Show a clear message instead of crashing.
        val supabaseReady = SupabaseProvider.isConfigured
        if (!supabaseReady) Log.e(TAG, "Supabase is not configured: add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties")

        setContent {
            MaryHairSalonTheme {
                if (supabaseReady) AppRoot() else SetupProblemScreen()
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
private fun SetupProblemScreen() {
    Column(
        Modifier.fillMaxSize().background(Brand.Bg).safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BrandMark(large = true)
        Text("Supabase is not set up", style = MaterialTheme.typography.headlineSmall)
        Text(
            "This build doesn't know which database to use.\n\n" +
                "1. Supabase dashboard → Project Settings → API\n" +
                "2. Open android/local.properties and add:\n" +
                "   SUPABASE_URL=https://<your-project>.supabase.co\n" +
                "   SUPABASE_ANON_KEY=<the anon public key>\n" +
                "   GOOGLE_WEB_CLIENT_ID=<web client id>.apps.googleusercontent.com\n" +
                "3. In Android Studio: File → Sync Project with Gradle Files, then run again.",
            color = Brand.TextDim,
        )
    }
}
