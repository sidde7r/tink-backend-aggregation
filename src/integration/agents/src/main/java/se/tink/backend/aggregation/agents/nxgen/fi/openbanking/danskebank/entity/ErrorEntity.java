package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class ErrorEntity {

    @Getter private String errorCode;
    private String message;
}
