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
        "This is the eIDAS QWAC certificate. The qualified certificate for electronic seal provide a common and eIDAS")
@JsonSchemaTitle("QWAC PEM")
@JsonSchemaExamples(
        "MIIF2zCCA8OgAwIBAgIJAIe7mjO1MVc5F...bg86WtFLXMkKobe7wihTzHuQnmDxj0n5mXJPokIL7Z+PIHZqx/1WraS")
@JsonSchemaInject(ints = {@JsonSchemaInt(path = "minLength", value = 100)})
@JsonProperty(required = true)
public @interface QWACConfiguration {}
