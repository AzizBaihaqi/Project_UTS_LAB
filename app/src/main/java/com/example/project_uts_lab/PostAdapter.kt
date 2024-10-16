package com.example.project_uts_lab

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class Post(
    var imageUrl: String?,
    var text: String,
    var userName: String,
    var avatarUrl: String?,
    var isPinned: Boolean = false, // Status apakah post dipin
    var timestamp: Long = System.currentTimeMillis(), // Tambahkan timestamp
    var isLiked: Boolean = false,  // Track whether the post is liked by the current user
    var likeCount: Int = 0,  // Store the number of likes
    var postId: String = "" // Store postId for Firestore operations
)

class PostAdapter(
    private val context: Context,
    private var postList: MutableList<Post>,
    private val onPinClick: (Post) -> Unit // Callback untuk menangani klik pin
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postImage: ImageView = view.findViewById(R.id.postImage)
        val postText: TextView = view.findViewById(R.id.postText)
        val userAvatar: ImageView = view.findViewById(R.id.userAvatar)
        val userName: TextView = view.findViewById(R.id.userName)
        val pinButton: ImageView = view.findViewById(R.id.pinButton)
        val likeButton: ImageView = view.findViewById(R.id.likeButton)
        val likeCountText: TextView = view.findViewById(R.id.likeCountText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        holder.userName.text = post.userName
        holder.postText.text = post.text
        holder.likeCountText.text = "${post.likeCount} Likes"

        // Load user avatar using Glide
        if (post.avatarUrl != null && post.avatarUrl!!.isNotEmpty()) {
            Glide.with(context)
                .load(post.avatarUrl)
                .circleCrop()
                .into(holder.userAvatar)
        } else {
            holder.userAvatar.setImageResource(R.drawable.circular_image) // Placeholder avatar
        }

        // Load post image if available
        if (post.imageUrl != null && post.imageUrl!!.isNotEmpty()) {
            holder.postImage.visibility = View.VISIBLE
            Glide.with(context).load(post.imageUrl).into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }

        // Pin/Unpin functionality
        holder.pinButton.setImageResource(
            if (post.isPinned) R.drawable.baseline_offline_pin_24 else R.drawable.baseline_push_pin_24
        )

        // Kirim event klik pin ke Fragment
        holder.pinButton.setOnClickListener {
            onPinClick(post) // Panggil callback untuk menangani logika pin di Fragment
        }

        // Like functionality
        holder.likeButton.setImageResource(
            if (post.isLiked) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24
        )
        holder.likeButton.setOnClickListener {
            toggleLikePost(post, holder)
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    // Hapus togglePinPost dari sini, pindahkan logikanya ke HomeFragment

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun toggleLikePost(post: Post, holder: PostViewHolder) {
        val userId = auth.currentUser?.uid ?: return  // Get the current user's ID
        val postRef = firestore.collection("posts").document(post.postId)

        if (post.isLiked) {
            // Unlike the post
            post.isLiked = false
            post.likeCount -= 1
            holder.likeButton.setImageResource(R.drawable.baseline_favorite_border_24)

            // Update Firestore: remove user from likedBy array and decrement the like count
            postRef.update(
                "likeCount", post.likeCount,
                "likedBy", FieldValue.arrayRemove(userId)
            )
        } else {
            // Like the post
            post.isLiked = true
            post.likeCount += 1
            holder.likeButton.setImageResource(R.drawable.baseline_favorite_24)

            // Update Firestore: add user to likedBy array and increment the like count
            postRef.update(
                "likeCount", post.likeCount,
                "likedBy", FieldValue.arrayUnion(userId)
            )
        }
        holder.likeCountText.text = "${post.likeCount} Likes"
    }
}

