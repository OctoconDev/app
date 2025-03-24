package app.octocon.app.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class AssumedUTCInstantSerializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Instant) {
    InstantIso8601Serializer.serialize(encoder, value)
  }

  override fun deserialize(decoder: Decoder): Instant {
    return Instant.parse(decoder.decodeString().let {
      if (it.endsWith("Z")) it
      else it + "Z"
    })
  }
}