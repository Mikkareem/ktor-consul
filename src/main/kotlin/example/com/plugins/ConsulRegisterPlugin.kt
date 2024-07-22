package example.com.plugins

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import kotlinx.coroutines.launch

class ConsulRegisterConfiguration {
    var consulUrl: String? = null
    var serviceName: String? = null
    var serviceId: String? = null
    var healthCheckUrl: String? = null
    var host: String? = "localhost"
}

val Consul = createApplicationPlugin(
    name = "Consul",
    createConfiguration = ::ConsulRegisterConfiguration
) {
    val consulUrl = (pluginConfig.consulUrl ?: environment?.config?.property("ktor.consul.register") ?: error("No Configuration available for Consul Register")).toString()
    val serviceName = (pluginConfig.serviceName ?: environment?.config?.property("ktor.consul.service_name") ?: error("No Configuration available for Consul Register")).toString()
    val serviceId = (pluginConfig.serviceId ?: environment?.config?.property("ktor.consul.service_id") ?: error("No Configuration available for Consul Register")).toString()
    val healthCheckUrl = (pluginConfig.healthCheckUrl ?: environment?.config?.property("ktor.consul.service_health_url") ?: error("No Configuration available for Consul Register")).toString()
    val host = (pluginConfig.host ?: environment?.config?.property("ktor.consul.host") ?: error("No Configuration available for Consul Register")).toString()
    val port = application.environment.config.port

    on(MonitoringEvent(ApplicationStarted)) {
        println("Host: " + it.environment.config.host)
        it.launch {
            HttpClient(Apache).use { client ->
                val response = client.put("${consulUrl.removeSuffix("/")}/v1/agent/service/register") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                            {
                                "ID": "$serviceId",
                                "Name": "$serviceName",
                                "Address": "$host",
                                "Port": $port,
                                "Check": {
                                    "HTTP": "http://$host:$port/${healthCheckUrl.removePrefix("/")}",
                                    "Interval": "10s",
                                    "TLSSkipVerify": true
                                }
                            }
                        """.trimIndent()
                    )
                }
                if(response.status != HttpStatusCode.OK) {
                    error("Cannot be registered to Consul")
                }
            }
        }
    }
}