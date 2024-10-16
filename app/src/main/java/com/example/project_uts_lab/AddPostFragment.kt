package com.example.project_uts_lab

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddPostFragment : Fragment() {

    private lateinit var selectedImageView: ImageView
    private lateinit var postTextInput: EditText
    private lateinit var shareButton: ImageView
    private lateinit var imageUploadIcon: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // Tambahkan autentikasi Firebase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        selectedImageView = view.findViewById(R.id.selectedImageView)
        postTextInput = view.findViewById(R.id.postTextInput)
        shareButton = view.findViewById(R.id.shareIcon)
        imageUploadIcon = view.findViewById(R.id.imageUploadIcon)

        imageUploadIcon.setOnClickListener {
            openGallery()
        }

        shareButton.setOnClickListener {
            uploadPost()
        }

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            selectedImageView.setImageURI(imageUri)
        }
    }

    private fun uploadPost() {
        val postText = postTextInput.text.toString().trim()  // Trim untuk menghapus spasi berlebih
        if (imageUri == null && postText.isEmpty()) {
            // Jika tidak ada gambar atau teks, tampilkan pesan error
            Toast.makeText(context, "Silakan tambahkan gambar atau teks sebelum mengunggah", Toast.LENGTH_SHORT).show()
            return  // Hentikan proses jika postingan kosong
        }

        if (imageUri != null) {
            val fileName = UUID.randomUUID().toString() // Nama file unik untuk gambar
            val imageRef = storageReference.child("images/$fileName")

            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    // Dapatkan URL gambar yang diunggah
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        savePostData(imageUrl) // Simpan URL gambar dan teks ke Firestore
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Gagal mengunggah gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Jika hanya teks yang diisi, unggah teks tanpa gambar
            savePostData(null)
        }
    }


    private fun savePostData(imageUrl: String?) {
        val postText = postTextInput.text.toString()
        val user = auth.currentUser

        if (user != null) {
            // Ambil nama pengguna dan avatar dari Firestore
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    val userName = document.getString("name") ?: "Unknown User"
                    val avatarUrl = document.getString("avatarUrl") ?: ""

                    // Map untuk menyimpan postingan
                    val postMap = hashMapOf(
                        "text" to postText,
                        "imageUrl" to imageUrl,
                        "userName" to userName, // Simpan nama pengguna
                        "avatarUrl" to avatarUrl, // Simpan URL avatar pengguna
                        "timestamp" to System.currentTimeMillis()
                    )

                    // Simpan postingan ke koleksi 'posts'
                    firestore.collection("posts")
                        .add(postMap)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Postingan berhasil diunggah", Toast.LENGTH_SHORT).show()
                            // Bersihkan input setelah berhasil unggah
                            postTextInput.text.clear()
                            selectedImageView.setImageResource(0)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Gagal mengunggah postingan: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Gagal mengambil data pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
