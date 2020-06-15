package se.tink.backend.aggregation.configuration.agents;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSchemaDescription(
        " The client_secret is a secret known only to the application and the authorization server.")
@JsonSchemaTitle("Client Secrets")
@JsonSchemaExamples("555d5513cb123456789050b90f06a18b21234567890a9cce977c5513734d3f41")
@JsonProperty(required = true)
public @interface ClientSecretsConfiguration {}
