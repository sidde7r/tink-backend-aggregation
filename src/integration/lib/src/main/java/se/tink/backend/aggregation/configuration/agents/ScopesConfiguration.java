package se.tink.backend.aggregation.configuration.agents;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaBool;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInt;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonProperty(required = true)
@JsonSchemaTitle("Scopes")
@JsonSchemaDescription(
        "These are the service scopes that PSD2 regulates. Payment initiation services (PIS) and account information services (AIS).")
@JsonSchemaInject(
        bools = {@JsonSchemaBool(path = "uniqueItems", value = true)},
        ints = {
            @JsonSchemaInt(path = "minItems", value = 1),
            @JsonSchemaInt(path = "maxItems", value = 2)
        },
        json =
                "{\n"
                        + "  \"items\" : {\n"
                        + "      \"enum\" : [\"AIS\",\"PIS\"]\n"
                        + "    }\n"
                        + "}")
@JsonSchemaExamples("AIS")
public @interface ScopesConfiguration {}
