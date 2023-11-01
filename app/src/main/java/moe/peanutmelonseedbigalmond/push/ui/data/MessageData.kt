package moe.peanutmelonseedbigalmond.push.ui.data

import android.os.Parcel
import android.os.Parcelable

data class MessageData(
    val type: String,
    val title: String,
    val content: String,
    val id: Long,
    val sendTime: Long,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong()
    )

    object Type {
        const val TEXT = "text"
        const val IMAGE = "image"
        const val MARKDOWN = "markdown"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeLong(id)
        parcel.writeLong(sendTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MessageData> {
        override fun createFromParcel(parcel: Parcel): MessageData {
            return MessageData(parcel)
        }

        override fun newArray(size: Int): Array<MessageData?> {
            return arrayOfNulls(size)
        }
    }
}