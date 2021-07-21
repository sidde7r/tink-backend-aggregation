package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
@Getter
public class AccountInfoEntity {
    @JsonProperty("iban")
    private final String iban;
}
