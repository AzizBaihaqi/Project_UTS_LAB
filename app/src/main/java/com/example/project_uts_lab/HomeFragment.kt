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

        postAdapter = PostAdapter(requireContext(), postList)
        recyclerView.adapter = postAdapter

        fetchPosts()

        return view
    }

    // Fetch posts and listen for user profile changes
    private fun fetchPosts() {
        val currentUser = auth.currentUser
        currentUser?.let {
            // Get current user ID
            val userId = it.uid
            firestore.collection("posts")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    postList.clear()
                    for (document in documents) {
                        val imageUrl = document.getString("imageUrl")
                        val text = document.getString("text") ?: ""
                        val userName = document.getString("userName") ?: "Unknown User"
                        val avatarUrl = document.getString("avatarUrl")

                        // Add posts to the post list
                        postList.add(Post(imageUrl, text, userName, avatarUrl))
                    }
                    postAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeFragment", "Error getting posts: ${exception.message}")
                }

            // Listen to changes in user profile (e.g. avatar or name updates)
            firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("HomeFragment", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val updatedUserName = snapshot.getString("name") ?: "Unknown User"
                        val updatedAvatarUrl = snapshot.getString("avatarUrl")

                        // Update the user's posts in real-time
                        updatePostsWithNewProfileData(updatedUserName, updatedAvatarUrl)
                    }
                }
        }
    }

    // Update posts with new user name and avatar
    private fun updatePostsWithNewProfileData(newUserName: String, newAvatarUrl: String?) {
        postList.forEach { post ->
            if (post.userName == auth.currentUser?.displayName) {
                post.userName = newUserName
                post.avatarUrl = newAvatarUrl
            }
        }
        postAdapter.notifyDataSetChanged() // Refresh the RecyclerView
    }
}
