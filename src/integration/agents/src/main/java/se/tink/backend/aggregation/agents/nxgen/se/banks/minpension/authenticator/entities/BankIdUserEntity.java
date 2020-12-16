package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BankIdUserEntity {
    private String firstname;
    private String lastname;
}
