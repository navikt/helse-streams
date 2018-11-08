package no.nav.helse.streams

import org.apache.kafka.common.serialization.*
import org.json.*

class JsonSerializer: Serializer<JSONObject> {

   override fun serialize(topic: String?, data: JSONObject?): ByteArray? {
      return data?.let {
         it.toString(0).toByteArray()
      }
   }

   override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) { }
   override fun close() { }

}
