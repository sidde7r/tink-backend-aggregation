package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ErrorEntity {
    private String code;
    private String message;
}
