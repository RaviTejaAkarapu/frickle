package com.example.messenger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_messagge.*

class NewMessaggeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_messagge)

        supportActionBar?.title = "Start New Chat"

        val adapter = GroupAdapter<ViewHolder>()

        adapter.add(UserItem())
        adapter.add(UserItem())
        adapter.add(UserItem())

        recyclerview_newmessage.adapter = adapter
    }
}

class UserItem : Item<ViewHolder>() {
    override fun getLayout(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
