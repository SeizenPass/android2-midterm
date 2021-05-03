package com.example.seizenplayer.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seizenplayer.R
import com.example.seizenplayer.models.MusicModel
import kotlinx.android.synthetic.main.music_list_item.*
import kotlinx.android.synthetic.main.music_list_item.view.*

open class MusicAdapter(
    private val context: Context,
    private var list: ArrayList<MusicModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.music_list_item,
                parent,
                false
            )
        )
    }


    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: MusicModel)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.tvTitle.text = model.songTitle
            holder.itemView.tvDescription.text = model.songArtist
            holder.itemView.fileName.text = model.title
            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}