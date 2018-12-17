package no.nav.helse.streams

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import org.apache.kafka.streams.KafkaStreams
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class StreamConsumer(val consumerName: String,
                     val streams: KafkaStreams,
                     val httpPort: Int = 8080) {

   private val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
   private val log = LoggerFactory.getLogger(consumerName)

   private val app = naisHttpChecks()

   fun start() {
      addShutdownHook()

      DefaultExports.initialize()
      app.start(wait = false)
      streams.start()
      log.info("Started stream consumer $consumerName")
   }

   fun stop(){
      streams.close()
      app.stop(5, 60, TimeUnit.SECONDS)
   }

   private fun naisHttpChecks() =
      embeddedServer(Netty, httpPort) {
         routing {

            get("/isalive") {
               if (!streams.state().isRunning) {
                  call.respondText("NOT ALIVE", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
               } else {
                  call.respondText("ALIVE", ContentType.Text.Plain)
               }
            }

            get("/isready") {
               call.respondText("READY", ContentType.Text.Plain)
            }

            get("/metrics") {
               val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: emptySet()
               call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                  TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
               }
            }
         }
      }

   private fun addShutdownHook() {
      streams.setUncaughtExceptionHandler{ _, ex ->
         log.error("Caught exception in stream, exiting", ex)
         stop()
      }
      Thread.currentThread().setUncaughtExceptionHandler { _, ex ->
         log.error("Caught exception, exiting", ex)
         stop()
      }

      Runtime.getRuntime().addShutdownHook(Thread {
         stop()
      })
   }

}
