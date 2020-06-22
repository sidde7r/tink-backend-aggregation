package se.tink.backend.aggregation.configuration.agents;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInt;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// Will be removed after TPA-607 is finished

@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSchemaDescription(
        "This is the eIDAS QSealC certificate. The qualified certificate for electronic seal provide a common and eIDAS")
@JsonSchemaTitle("QSealC PEM")
@JsonSchemaExamples(
        "MIIF2zCCA8OgAwIBAgIJAIe7mjO1QjavMA0GCSqGSIb3DQEBCw...yEfh9mZXOiupds73253UZO8QC")
@JsonSchemaInject(ints = {@JsonSchemaInt(path = "minLength", value = 100)})
@JsonProperty(required = true)
public @interface QSealCConfiguration {}
