package moe.peanutmelonseedbigalmond.push.notification

import android.os.Parcel
import android.os.Parcelable
import moe.peanutmelonseedbigalmond.push.emuration.MessageType

data class PushMessageNotificationBean(
    val id:Int,
    val title:String?,
    val coverImgUrl:String?,
    val content:String,
    val type:MessageType,
    val messageGroupId:String?,
    val messageGroupTitle:String?,
    val priority:Int,
):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readSerializable() as MessageType,
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(coverImgUrl)
        parcel.writeString(content)
        parcel.writeString(messageGroupId)
        parcel.writeString(messageGroupTitle)
        parcel.writeInt(priority)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PushMessageNotificationBean> {
        override fun createFromParcel(parcel: Parcel): PushMessageNotificationBean {
            return PushMessageNotificationBean(parcel)
        }

        override fun newArray(size: Int): Array<PushMessageNotificationBean?> {
            return arrayOfNulls(size)
        }
    }
}