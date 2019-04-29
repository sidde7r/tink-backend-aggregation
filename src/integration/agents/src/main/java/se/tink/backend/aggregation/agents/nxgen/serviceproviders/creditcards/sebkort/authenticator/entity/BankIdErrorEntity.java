package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdErrorEntity {
    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isBankIdAlreadyInProgress() {
        return SebKortConstants.Error.BANKID_ALREADY_IN_PROGRESS.equalsIgnoreCase(code);
    }
}
