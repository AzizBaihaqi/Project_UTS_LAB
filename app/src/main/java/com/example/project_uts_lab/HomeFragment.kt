package com.example.project_uts_lab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        postAdapter = PostAdapter(requireContext(), postList) { post ->
            togglePinPost(post) // Ketika pin diklik, jalankan logika di sini
        }
        recyclerView.adapter = postAdapter

        fetchPosts()

        return view
    }

    private fun fetchPosts() {
        val currentUser = auth.currentUser?.uid ?: return  // Dapatkan user ID

        firestore.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { postDocuments ->
                postList.clear()
                for (postDocument in postDocuments) {
                    val postId = postDocument.id
                    val imageUrl = postDocument.getString("imageUrl")
                    val text = postDocument.getString("text") ?: ""
                    val userName = postDocument.getString("userName") ?: "Unknown User"
                    val avatarUrl = postDocument.getString("avatarUrl")
                    val likeCount = postDocument.getLong("likeCount")?.toInt() ?: 0
                    val likedBy = postDocument.get("likedBy") as? List<String> ?: emptyList<String>()
                    val timestamp = postDocument.getTimestamp("timestamp")?.toDate()?.time ?: System.currentTimeMillis()

                    // Cek apakah postingan sudah di-pin oleh user saat ini
                    firestore.collection("pinnedPosts")
                        .whereEqualTo("userId", currentUser)
                        .whereEqualTo("postId", postId)
                        .get()
                        .addOnSuccessListener { pinDocuments ->
                            val isPinned = !pinDocuments.isEmpty()

                            // Buat objek Post dan tambahkan ke list
                            postList.add(Post(imageUrl, text, userName, avatarUrl, isPinned, timestamp, likedBy.contains(currentUser), likeCount, postId))

                            // Sort pinned posts first, then by descending timestamp
                            postList.sortWith(
                                compareByDescending<Post> { it.isPinned }
                                    .thenByDescending { it.timestamp }
                            )

                            postAdapter.notifyDataSetChanged()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error fetching posts: ${e.message}")
            }
    }

    // Tambahkan fungsi togglePinPost di sini
    private fun togglePinPost(post: Post) {
        val currentUser = auth.currentUser?.uid ?: return  // Pastikan pengguna sudah login
        val pinRef = firestore.collection("pinnedPosts")
            .whereEqualTo("userId", currentUser)
            .whereEqualTo("postId", post.postId)

        pinRef.get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Post sudah di-pin oleh user ini, maka lakukan unpin
                    for (document in documents) {
                        firestore.collection("pinnedPosts").document(document.id).delete()
                    }
                    post.isPinned = false
                } else {
                    // Post belum di-pin, maka lakukan pin
                    val pinData = hashMapOf(
                        "userId" to currentUser,
                        "postId" to post.postId
                    )
                    firestore.collection("pinnedPosts").add(pinData)
                    post.isPinned = true
                }

                // Lakukan sort ulang setelah pin/unpin
                postList.sortWith(
                    compareByDescending<Post> { it.isPinned } // Sort pinned posts first
                        .thenByDescending { it.timestamp }   // Then sort by timestamp for unpinned posts
                )
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("PinPost", "Error handling pin/unpin: ${e.message}")
            }
    }
}


