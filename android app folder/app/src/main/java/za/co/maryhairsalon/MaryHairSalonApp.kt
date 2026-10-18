package za.co.maryhairsalon

import android.app.Application
import za.co.maryhairsalon.di.AppContainer

class MaryHairSalonApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
