package com.example.brightbridgetask.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.brightbridgetask.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    private lateinit var bindingForSplashScreen: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingForSplashScreen = ActivitySplashScreenBinding.inflate(layoutInflater)
        val view = bindingForSplashScreen.root
        setContentView(view)


        Handler(Looper.getMainLooper()).postDelayed({
            val redirectHome = Intent(this@SplashScreen, MainActivity::class.java)
            startActivity(redirectHome)
            finish()
        }, 3000)


    }
}