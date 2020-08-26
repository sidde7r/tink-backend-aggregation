package se.tink.backend.aggregation.configuration.agents;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonProperty(required = true)
@JsonSchemaExamples("4578616d706c652c2074657374696e67")
@JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-f]{32}$")})
public @interface HexString32Configuration {}
