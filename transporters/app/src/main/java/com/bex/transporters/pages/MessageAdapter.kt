package com.bex.transporters.pages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bex.transporters.R
import com.bex.transporters.models.Message

class MessageAdapter (private var messages: List<Message>, val currentUserId : Int): RecyclerView.Adapter<MessageAdapter.BaseViewHolder>() {


    fun updateData(newData: List<Message>) {
        this.messages = newData
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return messages.size
    }
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.sender_id == currentUserId) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }
    abstract class BaseViewHolder(view: View): RecyclerView.ViewHolder(view) {
        abstract fun bind(message: Message)
    }

    class SentMessageHolder(view: View): BaseViewHolder(view) {
        private val messageSent = view.findViewById<TextView>(R.id.msgsenttext)

        override fun bind(message: Message) {
            messageSent.text = message.message_text
        }
    }

    class ReceivedMessageHolder(view: View): BaseViewHolder(view) {
        private val messageReceived = view.findViewById<TextView>(R.id.msgreceivedtext)

        override fun bind(message: Message) {
            messageReceived.text = message.message_text
        }
    }



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val view: View
            return if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                SentMessageHolder(view)
            } else { // viewType == VIEW_TYPE_MESSAGE_RECEIVED
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageHolder(view)
            }
        }

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            val message = messages[position]
            holder.bind(message)
        }



    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
    }

}
