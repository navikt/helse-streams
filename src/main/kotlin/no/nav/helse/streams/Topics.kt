package no.nav.helse.streams

import org.apache.kafka.common.serialization.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.*

object Topics {
   val SYKEPENGESØKNADER_INN = Topic(
      name = "syfo-soknad-v1",
      keySerde = Serdes.String(),
      valueSerde = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())
   )

   val SYKEPENGEBEHANDLING = Topic(
      name = "privat-sykepengebehandling",
      keySerde = Serdes.String(),
      valueSerde = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())
   )

   val VEDTAK_INFOTRYGD = Topic(
      name = "privat-helse-infotrygd-vedtak",
      keySerde = Serdes.String(),
      valueSerde = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())

   )

   val VEDTAK_SYKEPENGER = Topic(
      name = "aapen-helse-sykepenger-vedtak",
      keySerde = Serdes.String(),
      valueSerde = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())
   )
   val VEDTAK_KOMBINERT = Topic(
      name = "privat-helse-vedtak-kombinert",
      keySerde = Serdes.String(),
      valueSerde = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())
   )

   val VEDTAK_RESULTAT = Topic(
      name = "privat-helse-vedtak-resultat",
      keySerde = Serdes.String(),
      valueSerde = Serdes.String()
   )
}

fun <K: Any, V: Any> StreamsBuilder.consumeTopic(topic: Topic<K, V>): KStream<K, V> {
   return stream<K, V>(
      topic.name, Consumed.with(topic.keySerde, topic.valueSerde)
   )
}

fun <K, V> KStream<K, V>.toTopic(topic: Topic<K, V>) {
   return to(topic.name, Produced.with(topic.keySerde, topic.valueSerde))
}
