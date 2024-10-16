package com.example.project_uts_lab

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

        // Cek apakah pengguna sudah login
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Pengguna sudah login
            Log.d("MainActivity", "Pengguna sudah login: ${currentUser.email}")

            // Inisialisasi BottomNavigationView
            bottomNavigationView = findViewById(R.id.bottom_navigation)

            // Muat HomeFragment jika belum ada fragment yang dimuat
            if (savedInstanceState == null) {
                loadFragment(HomeFragment())
                // Set tombol Home sebagai aktif
                bottomNavigationView.selectedItemId = R.id.nav_home
            }

            // Set listener untuk BottomNavigationView
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
            // Pengguna belum login, arahkan ke LoginActivity
            Log.d("MainActivity", "Pengguna belum login")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Tutup MainActivity setelah login
        }
    }

    // Fungsi untuk memuat fragment
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
