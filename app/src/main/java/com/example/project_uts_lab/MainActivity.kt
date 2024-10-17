package com.example.project_uts_lab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Check login status
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // If not logged in, go to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close MainActivity
        } else {
            // If logged in, load the fragments
            val currentUser = auth.currentUser
            if (currentUser != null) {
                Log.d("MainActivity", "User is logged in: ${currentUser.email}")

                // Initialize BottomNavigationView
                bottomNavigationView = findViewById(R.id.bottom_navigation)

                // Load HomeFragment if no fragment is loaded
                if (savedInstanceState == null) {
                    loadFragment(HomeFragment())
                    bottomNavigationView.selectedItemId = R.id.nav_home // Set Home as active
                }

                // Set listener for BottomNavigationView
                bottomNavigationView.setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.nav_home -> {
                            loadFragment(HomeFragment())
                            true
                        }
                        R.id.nav_profile -> {
                            loadFragment(ProfileFragment())
                            true
                        }
                        R.id.nav_add -> {
                            loadFragment(AddPostFragment())
                            true
                        }
                        else -> false
                    }
                }
            } else {
                // If no user is logged in, redirect to LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
