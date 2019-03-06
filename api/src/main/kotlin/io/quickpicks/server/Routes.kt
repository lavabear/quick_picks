package io.quickpicks.server

import graphql.ExecutionResult
import graphql.GraphQL

import io.quickpicks.Metrics
import io.quickpicks.api.*
import io.quickpicks.extensions.mark
import io.quickpicks.graphql.GraphqlRequest

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Routes(private val metrics: Metrics, private val graphQL: GraphQL) {
    fun status() : Status = metrics.statusCount.mark {
        val healthStatus = metrics.healthCheck.get()
        val statusType = if (healthStatus.filter { (_, v) -> !v.isHealthy }.isNotEmpty())
            StatusType.WARN
        else
            StatusType.OK
        Status(statusType, healthStatus).also { LOG.info("Current Status - {}", it) }
    }

    fun graphql(request: GraphqlRequest) : ExecutionResult = metrics.requestTimer.time {
         graphQL.execute(request.prepared())
    }

    fun error404() : SimpleMessage = metrics.meter404.mark(::notFound)

    fun error500() : SimpleMessage = metrics.meter500.mark(::errorMessage)

    companion object {
        private val LOG : Logger = LoggerFactory.getLogger(Routes::class.java)
    }
}