package com.yigitusq.orderservice.config;

import brave.handler.SpanHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.brave.ZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@Configuration
public class TracingConfig {

    @Value("${management.zipkin.tracing.endpoint:http://localhost:9411/api/v2/spans}")
    private String zipkinEndpoint;

    @Bean
    public SpanHandler zipkinSpanHandler() {
        var sender = URLConnectionSender.create(zipkinEndpoint);
        var reporter = AsyncReporter.builder(sender).build();
        return ZipkinSpanHandler.create(reporter);
    }
}