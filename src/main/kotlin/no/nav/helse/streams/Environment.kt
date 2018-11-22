package no.nav.helse.streams

data class Environment(
   val username: String = getRequiredEnvVar("KAFKA_PASSWORD"),
   val password: String = getRequiredEnvVar("KAFKA_PASSWORD"),
   val bootstrapServersUrl: String = getRequiredEnvVar("KAFKA_BOOTSTRAP_SERVERS"),
   val schemaRegistryUrl: String = getRequiredEnvVar("KAFKA_SCHEMA_REGISTRY_URL"),
   val httpPort: Int? = null,
   val navTruststorePath: String? = getEnvVar("NAV_TRUSTSTORE_PATH"),
   val navTruststorePassword: String? = getEnvVar("NAV_TRUSTSTORE_PASSWORD"),
   val stateDir: String? = getEnvVar("STATE_DIR")
   )

private fun getRequiredEnvVar(varName: String) =
   getEnvVar(varName) ?: getSystemProperty(varName) ?: throw RuntimeException("Missing required variable '$varName'")

private fun getEnvVar(varName: String) = System.getenv(varName)

private fun getSystemProperty(varName: String) = System.getProperty(varName)
