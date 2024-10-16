package com.example.project_uts_lab

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

data class Post(
    var imageUrl: String?,
    var text: String,
    var userName: String,
    var avatarUrl: String?
)

class PostAdapter(private val context: Context, private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postImage: ImageView = view.findViewById(R.id.postImage)
        val postText: TextView = view.findViewById(R.id.postText)
        val userAvatar: ImageView = view.findViewById(R.id.userAvatar)
        val userName: TextView = view.findViewById(R.id.userName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        // Set user name
        holder.userName.text = post.userName

        // Load avatar image using Glide or set a default placeholder
        if (post.avatarUrl != null && post.avatarUrl!!.isNotEmpty()) {
            Glide.with(context)
                .load(post.avatarUrl)
                .circleCrop()  // Circular avatar image
                .into(holder.userAvatar)
        } else {
            holder.userAvatar.setImageResource(R.drawable.circular_image) // Placeholder avatar
        }

        // Set the post text
        holder.postText.text = post.text

        // Load post image if available
        if (post.imageUrl != null && post.imageUrl!!.isNotEmpty()) {
            holder.postImage.visibility = View.VISIBLE
            Glide.with(context)
                .load(post.imageUrl)
                .into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}
