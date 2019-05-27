package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.entity;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailedConsentAccessEntity implements ConsentAccessEntity {
    private List<ConsentAccessIdentifier> accounts;
    private List<ConsentAccessIdentifier> balances;
    private List<ConsentAccessIdentifier> transactions;

    public DetailedConsentAccessEntity(
            List<ConsentAccessIdentifier> accounts,
            List<ConsentAccessIdentifier> balances,
            List<ConsentAccessIdentifier> transactions) {
        this.accounts = accounts;
        this.balances = balances;
        this.transactions = transactions;
    }

    public DetailedConsentAccessEntity(List<String> ibans) {

        List<ConsentAccessIdentifier> consentAccessIdentifiers =
                ibans.stream().map(ConsentAccessIdentifier::new).collect(Collectors.toList());

        this.accounts = consentAccessIdentifiers;
        this.balances = consentAccessIdentifiers;
        this.transactions = consentAccessIdentifiers;
    }
}
