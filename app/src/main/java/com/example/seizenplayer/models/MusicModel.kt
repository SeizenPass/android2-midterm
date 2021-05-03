package com.example.seizenplayer.models

import java.io.Serializable

data class MusicModel(
    var title: String,
    var resId: Int,
    var songTitle: String,
    var songArtist: String,
    var songAlbum: String,
) : Serializable