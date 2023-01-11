package com.r3.developers.csdetemplate.utxo.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kong.unirest.Unirest
import net.corda.flow.rpcops.v1.types.request.StartFlowParameters
import net.corda.flow.rpcops.v1.types.response.FlowStatusResponse
import net.corda.flow.rpcops.v1.types.response.FlowStatusResponses
import java.io.IOException
import java.util.*

//TODO: emko: retry
//TODO: emko: error handling
//TODO: emko: logging
class FlowService {

    companion object {
        var path = "flow"

        var mapper: ObjectMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        init {
            //TODO: emko: parametrize:
            Unirest.config()
                .verifySsl(false)
                .setDefaultBasicAuth("admin", "admin")
                .defaultBaseUrl("https://localhost:8888/api/v1/")
            //TODO: emko: look into:
//                .objectMapper = JacksonObjectMapper(mapper)
        }

        /*
        /flow/{holdingidentityshorthash}
        This method returns an array containing the statuses of all flows running for a specified holding identity.
        An empty array is returned if there are no flows running.
         */
        fun listFlows(holdingIdentityShortHash: String): FlowStatusResponses {
            val request = Unirest
                .get("$path/$holdingIdentityShortHash")
            val response = request.asString()
            if (response.isSuccess) {
                return mapper.readValue(response.body, FlowStatusResponses::class.java)
            }
            throw IOException("${request.url} => ${response.statusText}")
        }

        /*
        /flow/{holdingidentityshorthash}/{clientrequestid}
        This method gets the current status of the specified flow instance.
         */
        fun getFlow(holdingIdentityShortHash: String, clientRequestId: String): FlowStatusResponse {
            val request = Unirest
                .get("$path/$holdingIdentityShortHash/$clientRequestId")
            val response = request.asString()
            if (response.isSuccess) {
                return mapper.readValue(response.body, FlowStatusResponse::class.java)
            }
            throw IOException("${request.url} => ${response.statusText}")
        }

        /*
        /flow/{holdingidentityshorthash}
        This method starts a new instance for the specified flow for the specified holding identity.
         */
        fun startFlow(holdingIdentityShortHash: String, startFlowParameters: StartFlowParameters): FlowStatusResponse {
            val request = Unirest
                .post("$path/$holdingIdentityShortHash")
                .body(mapper.writeValueAsString(startFlowParameters))
            val response = request.asString()
            if (response.isSuccess) {
                return mapper.readValue(response.body, FlowStatusResponse::class.java)
            }
            throw IOException("${request.url} => ${response.statusText}")
        }

        fun startFlow(holdingIdentityShortHash: String, flowClassName: String, requestData: Any?): FlowStatusResponse {
            val startFlowParameters: StartFlowParameters = StartFlowParameters(
                "${flowClassName.split(".").last()}-${UUID.randomUUID()}",
                flowClassName,
                requestData
            )
            return startFlow(holdingIdentityShortHash, startFlowParameters)
        }
    }
}