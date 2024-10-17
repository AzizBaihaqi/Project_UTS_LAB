package com.example.project_uts_lab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var signupRedirectText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if the user is already logged in
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // If logged in, redirect to MainActivity
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        auth = FirebaseAuth.getInstance()
        loginEmail = findViewById(R.id.login_email)
        loginPassword = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        signupRedirectText = findViewById(R.id.signupRedirectText)

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString()
            val pass = loginPassword.text.toString()

            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (pass.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener {
                            // Save login status in SharedPreferences
                            val editor = sharedPref.edit()
                            editor.putBoolean("isLoggedIn", true)
                            editor.putString("userEmail", email)
                            editor.apply()

                            Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish() // Close LoginActivity
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    loginPassword.error = "Password cannot be empty"
                }
            } else if (email.isEmpty()) {
                loginEmail.error = "Email cannot be empty"
            } else {
                loginEmail.error = "Please enter a valid email"
            }
        }

        signupRedirectText.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
        }
    }
}
