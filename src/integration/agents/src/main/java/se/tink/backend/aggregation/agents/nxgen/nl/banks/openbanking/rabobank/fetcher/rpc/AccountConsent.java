package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import lombok.Getter;
import lombok.ToString;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@ToString
public class AccountConsent {

    private String iban;
    private String currency;
    private String status;
    private String validUntil;
}
