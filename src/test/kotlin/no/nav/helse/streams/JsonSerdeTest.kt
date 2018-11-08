package no.nav.helse.streams

import org.amshove.kluent.*
import org.jetbrains.spek.api.*
import org.jetbrains.spek.api.dsl.*
import org.json.*

object JsonSerdeTest: Spek({

   describe("deserialize sykepengesøknad msgs") {
      given("null input") {
         it("returns null"){
            val deserialized = JsonDeserializer().deserialize("my-topic", null)
            deserialized.shouldBeNull()
         }
      }

      given("non-null input") {
         on("valid json") {
            it("returns JSONObject"){
               val deserialized = JsonDeserializer().deserialize("my-topic", completeJson.toByteArray())
               deserialized.shouldNotBeNull()
            }
         }

         on("invalid json") {
            it("returns null"){
               val deserialized = JsonDeserializer().deserialize("my-topic", "bogus json".toByteArray())
               deserialized.shouldBeNull()
            }
         }

      }
   }

   describe("serialize sykepengesøknad msgs") {
      given("null input") {
         it("returns null"){
            val serialized = JsonSerializer().serialize("my-topic", null)
            serialized.shouldBeNull()
         }
      }

      given("non-null input") {
         it("returns a byte array containing json"){
            val serialized = JsonSerializer().serialize("my-topic", JSONObject(completeJson))
            serialized.shouldNotBeNull()
         }
      }
   }

})

const val completeJson = "{\"id\":\"id\"," +
   "\"aktorId\":\"aktorId\"," +
   "\"sykmeldingId\":\"sykmeldingId\"," +
   "\"soknadstype\":\"soknadstype\"," +
   "\"status\":\"status\"," +
   "\"fom\":\"2018-10-15\"," +
   "\"tom\":\"2018-10-15\"," +
   "\"opprettetDato\":\"2018-10-15\"," +
   "\"innsendtDato\":\"2018-10-15\"," +
   "\"sporsmal\":[{\"id\":\"id\"," +
   "\"tag\":\"tag\"," +
   "\"sporsmalstekst\":\"sporsmalstekst\"," +
   "\"undertekst\":\"undertekst\"," +
   "\"svartype\":\"svartype\"," +
   "\"min\":\"min\"," +
   "\"max\":\"max\"," +
   "\"kriterieForVisningAvUndersporsmal\":\"kriterieForVisningAvUndersporsmal\"," +
   "\"svar\":[{\"verdi\":\"svarverdi\"}]," +
   "\"undersporsmal\":[{\"id\":\"id\"," +
   "\"tag\":\"tag\"," +
   "\"sporsmalstekst\":\"sporsmalstekst\"," +
   "\"undertekst\":\"undertekst\"," +
   "\"svartype\":\"svartype\"," +
   "\"min\":\"min\"," +
   "\"max\":\"max\"," +
   "\"kriterieForVisningAvUndersporsmal\":\"kriterieForVisningAvUndersporsmal\"," +
   "\"svar\":[{\"verdi\":\"undersporsmal.svarverdi\"}]," +
   "\"undersporsmal\":[]}]}]," +
   "\"korrigerer\":\"korrigerer\"," +
   "\"korrigertAv\":\"korrigertAv\"}"
