package no.nav.helse.streams

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced

private val strings = Serdes.String()
private val json = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())

object Topics {

   /**
    * Legacy-input to the whole process. Contains the original application.
    */
   val SYKEPENGESØKNADER_INN_LEGACY = Topic(
   name = "syfo-soknad-v1",
   keySerde = strings,
   valueSerde = json
   )

   /**
    * Input to the whole process. Contains the original application.
    */
   val SYKEPENGESØKNADER_INN = Topic(
      name = "syfo-soknad-v2",
      keySerde = strings,
      valueSerde = json
   )

   /**
    * Produced by `splitt` and `spinne`, (at least) consumed by `spinne`.
    *
    * Contains applications enriched with supporting information.
    */
   val SYKEPENGEBEHANDLING = Topic(
      name = "privat-sykepengebehandling",
      keySerde = Serdes.String(),
      valueSerde = json
   )

   val VEDTAK_INFOTRYGD = Topic(
      name = "privat-helse-infotrygd-vedtak",
      keySerde = strings,
      valueSerde = json
   )

   val VEDTAK_SYKEPENGER = Topic(
      name = "aapen-helse-sykepenger-vedtak",
      keySerde = strings,
      valueSerde = json
   )
   val VEDTAK_KOMBINERT = Topic(
      name = "privat-helse-vedtak-kombinert",
      keySerde = strings,
      valueSerde = json
   )

   val VEDTAK_RESULTAT = Topic(
      name = "privat-helse-vedtak-resultat",
      keySerde = strings,
      valueSerde = strings
   )

   val SYKEPENGEBEHANDLINGSFEIL = Topic(
      name = "privat-helse-sykepenger-behandlingsfeil",
      keySerde = Serdes.String(),
      valueSerde = json
   )

}

fun <K : Any, V : Any> StreamsBuilder.consumeTopic(topic: Topic<K, V>): KStream<K, V> {
   return consumeTopic(topic, null)
}

fun <K: Any, V: Any> StreamsBuilder.consumeTopic(topic: Topic<K, V>, schemaRegistryUrl: String?): KStream<K, V> {
   schemaRegistryUrl?.let {
      topic.keySerde.configure(mapOf(
         AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl
      ), true)

      topic.valueSerde.configure(mapOf(
         AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl
      ), false)
   }

   return stream<K, V>(
      topic.name, Consumed.with(topic.keySerde, topic.valueSerde)
   )
}

fun <K, V> KStream<K, V>.toTopic(topic: Topic<K, V>) {
   return to(topic.name, Produced.with(topic.keySerde, topic.valueSerde))
}
