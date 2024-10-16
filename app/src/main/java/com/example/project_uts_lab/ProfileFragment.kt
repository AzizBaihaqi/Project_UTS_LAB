package com.example.project_uts_lab

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var avatarImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var nimTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var signOutButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inisialisasi View
        avatarImageView = view.findViewById(R.id.avatarImageView)
        usernameTextView = view.findViewById(R.id.usernameTextView)
        nimTextView = view.findViewById(R.id.nimTextView)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        signOutButton = view.findViewById(R.id.signOutButton)

        // Inisialisasi Firebase Auth dan Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        fetchUserData()

        // Fungsi untuk Sign Out
        signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish() // Tutup ProfileFragment setelah logout
        }

        // Fungsi untuk Edit Profile
        editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        return view
    }
    override fun onResume() {
        super.onResume()
        // Fetch user data when fragment resumes
        fetchUserData()
    }

    // Mengambil data pengguna dari Firestore
    private fun fetchUserData() {
        val currentUser = auth.currentUser
        currentUser?.let {
            val userId = it.uid

            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("name")
                        val nim = document.getString("nim")
                        val avatarUrl = document.getString("avatarUrl")

                        // Set data ke dalam TextView dan ImageView
                        usernameTextView.text = username ?: "Nama tidak ditemukan"
                        nimTextView.text = nim ?: "NIM tidak ditemukan"

                        // Jika ada URL avatar, muat gambar menggunakan Glide dengan CircleCrop
                        if (avatarUrl != null) {
                            Glide.with(this)
                                .load(avatarUrl)
                                .transform(CircleCrop()) // Membuat gambar berbentuk lingkaran
                                .into(avatarImageView)
                        } else {
                            avatarImageView.setImageResource(R.drawable.circular_image) // Gambar default jika avatar tidak ada
                        }
                    }
                }
        }
    }
}
