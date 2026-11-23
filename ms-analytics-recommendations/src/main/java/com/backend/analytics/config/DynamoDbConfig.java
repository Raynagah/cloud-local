package com.backend.analytics.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

@Configuration
public class DynamoDbConfig {

    /**
     * Cliente de Amazon DynamoDB.
     *  - Sin endpoint  => AWS real (credenciales: variables AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY, rol IAM, etc.)
     *  - Con endpoint  => DynamoDB Local (docker-compose) con credenciales ficticias.
     */
    @Bean
    public DynamoDbClient dynamoDbClient(
            @Value("${analytics.dynamodb.endpoint:}") String endpoint,
            @Value("${analytics.dynamodb.region:us-east-1}") String region) {

        DynamoDbClientBuilder builder = DynamoDbClient.builder().region(Region.of(region));

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint))
                   .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local")));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }
}
