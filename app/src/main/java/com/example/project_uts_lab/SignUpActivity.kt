package com.example.project_uts_lab

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupConfirmPassword: EditText
    private lateinit var signupButton: Button
    private lateinit var loginRedirectText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Handle window insets (edge-to-edge display)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        signupEmail = findViewById(R.id.signup_email)
        signupPassword = findViewById(R.id.signup_password)
        signupConfirmPassword = findViewById(R.id.signup_confirm_password)
        signupButton = findViewById(R.id.signup_button)
        loginRedirectText = findViewById(R.id.loginRedirectText)

        signupButton.setOnClickListener {
            val email = signupEmail.text.toString().trim()
            val pass = signupPassword.text.toString().trim()
            val confirmPass = signupConfirmPassword.text.toString().trim()

            when {
                email.isEmpty() -> {
                    signupEmail.error = "Email Cannot Be Empty"
                }
                pass.isEmpty() -> {
                    signupPassword.error = "Password Cannot Be Empty"
                }
                confirmPass.isEmpty() -> {
                    signupConfirmPassword.error = "Confirm Password Cannot Be Empty"
                }
                pass != confirmPass -> {
                    signupConfirmPassword.error = "Passwords do not match"
                }
                else -> {
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@SignUpActivity, "Signup Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                        } else {
                            Toast.makeText(this@SignUpActivity, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        loginRedirectText.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
        }
    }
}
