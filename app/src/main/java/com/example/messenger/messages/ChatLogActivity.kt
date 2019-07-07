package com.example.messenger.messages

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.messenger.R
import com.example.messenger.model.ChatMessage
import com.example.messenger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    lateinit var friendUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter

        friendUser = intent.getParcelableExtra<User>(NewMessaggeActivity.USER_KEY)
        supportActionBar?.title = friendUser.username
        Log.d(TAG, "USERRRRR = ${friendUser.username}")
//        imageViewFrom.setImageURI(Uri.parse(user.profileImageUri))

//        setupDummyData()
        listenForMessages()
        send_button_chat_log.setOnClickListener {
            Log.d(TAG, "send button clicked...")
            performSendMessage()
            edittext_chat_log.text.clear()
            recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = friendUser.uid
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        reference.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, currentUser!!))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, friendUser))
                    }
                }

                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage() {
        val text = edittext_chat_log.text.toString()
//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = friendUser.uid
        val timestamp = (System.currentTimeMillis()) / 1000

        if (fromId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, timestamp)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "saved our chat message : ${reference.key}")
            }
        toReference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "saved our chat message : ${reference.key}")
            }

        val latestMessageReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageReference.setValue(chatMessage)

val latestMessageToReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToReference.setValue(chatMessage)

    }

//    private fun setupDummyData() {
//        val adapter = GroupAdapter<ViewHolder>()
//
//        adapter.add(ChatFromItem("From Message......"))
//        adapter.add(ChatToItem("To message, just making a little bit longer..."))
//        adapter.add(ChatFromItem("From Message......"))
//        adapter.add(ChatToItem("To message, just making a little bit longer..."))
//        adapter.add(ChatFromItem("From Message......"))
//        adapter.add(ChatToItem("To message, just making a little bit longer..."))
//
//        recyclerview_chat_log.adapter = adapter
//    }

    companion object {
        val TAG = "CHAT_LOG_ACTIVITY"
    }
}

class ChatFromItem(val text: String?, val currentUser: User) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textViewFrom.text = text
        Picasso.get()
            .load(currentUser.profileImageUri)
            .into(viewHolder.itemView.imageViewFrom)
    }
}

class ChatToItem(val text: String?, val friendUser: User) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textViewTo.text = text
        Picasso.get()
            .load(friendUser.profileImageUri)
            .into(viewHolder.itemView.imageViewTo)
    }
}
