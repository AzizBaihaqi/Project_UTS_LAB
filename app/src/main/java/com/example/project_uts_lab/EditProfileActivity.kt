package com.example.project_uts_lab

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var nimInput: EditText
    private lateinit var avatarImage: ImageView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private var selectedImageUri: Uri? = null
    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var oldUserName: String? = null
    private var oldAvatarUrl: String? = null  // Menyimpan avatar lama

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        nameInput = findViewById(R.id.nameInput)
        nimInput = findViewById(R.id.nimInput)
        avatarImage = findViewById(R.id.avatarImage)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

        // Ketika avatarImage diklik, buka galeri untuk memilih gambar
        avatarImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        saveButton.setOnClickListener {
            saveProfile()
        }

        cancelButton.setOnClickListener {
            finish() // Tutup halaman Edit Profile
        }

        loadUserProfile() // Memuat profil pengguna
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userName = document.getString("name") ?: ""
                        val nim = document.getString("nim") ?: ""
                        val avatarUrl = document.getString("avatarUrl") ?: ""

                        oldUserName = userName
                        oldAvatarUrl = avatarUrl // Simpan avatar lama

                        nameInput.setText(userName)
                        nimInput.setText(nim)
                        if (avatarUrl.isNotEmpty()) {
                            Glide.with(this)
                                .load(avatarUrl)
                                .circleCrop()
                                .into(avatarImage)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memuat profil: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfile() {
        val name = nameInput.text.toString()
        val nim = nimInput.text.toString()
        val user = auth.currentUser

        // Validasi NIM agar harus berisi 11 digit
        if (nim.length != 11 || !nim.all { it.isDigit() }) {
            nimInput.error = "NIM harus berisi tepat 11 digit angka"
            return
        }

        if (user != null && name.isNotEmpty()) {
            val userData = hashMapOf(
                "name" to name,
                "nim" to nim
            )

            // Jika avatar baru dipilih, upload dan perbarui URL avatar
            if (selectedImageUri != null) {
                val avatarRef = storageReference.child("avatars/${user.uid}.jpg")
                avatarRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener {
                        avatarRef.downloadUrl.addOnSuccessListener { uri ->
                            val avatarUrl = uri.toString()
                            userData["avatarUrl"] = avatarUrl

                            // Simpan profil dengan avatar baru
                            updateUserProfile(user.uid, userData, name, avatarUrl)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal mengunggah avatar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Jika tidak ada avatar baru yang dipilih, gunakan avatar lama
                firestore.collection("users").document(user.uid).get()
                    .addOnSuccessListener { document ->
                        val oldAvatarUrl = document.getString("avatarUrl") ?: ""

                        userData["avatarUrl"] = oldAvatarUrl  // Tetapkan avatar lama

                        // Simpan profil dengan avatar lama
                        updateUserProfile(user.uid, userData, name, oldAvatarUrl)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal memuat avatar lama: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Nama dan NIM harus diisi", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateUserProfile(userId: String, userData: HashMap<String, String>, newUserName: String, newAvatarUrl: String) {
        firestore.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                updatePostUserDetails(newUserName, newAvatarUrl) // Update data posting
                Toast.makeText(this, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke halaman profil
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePostUserDetails(newUserName: String, newAvatarUrl: String) {
        // Update nama dan avatar di semua postingan pengguna
        firestore.collection("posts")
            .whereEqualTo("userName", oldUserName)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val postRef = firestore.collection("posts").document(document.id)
                    postRef.update("userName", newUserName, "avatarUrl", newAvatarUrl)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui nama pada postingan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            Glide.with(this)
                .load(selectedImageUri)
                .circleCrop()
                .into(avatarImage)
        }
    }
}
