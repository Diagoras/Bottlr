package com.bottlr.app

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.time.Instant

object InstantParceler : Parceler<Instant?> {
    override fun create(parcel: Parcel): Instant? {
        val millis = parcel.readLong()
        return if (millis == Long.MIN_VALUE) null else Instant.ofEpochMilli(millis)
    }

    override fun Instant?.write(parcel: Parcel, flags: Int) {
        parcel.writeLong(this?.toEpochMilli() ?: Long.MIN_VALUE)
    }
}

@Parcelize
@TypeParceler<Instant?, InstantParceler>
data class Bottle(
    var name: String,
    var distillery: String,
    var type: String,
    var abv: String,
    var age: String,
    var photoUri: Uri?,
    var notes: String,
    var region: String,
    var keywords: String,
    var rating: String,
    var bottleID: String? = null,
    var createdAt: Instant? = null
) : Parcelable
