package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DailyLimitWithoutConfirmationLoginEntity {
    private String currency;
    private String value;
}
