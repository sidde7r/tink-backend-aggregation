package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.savingsaccount.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingsAccountResponse {
    private List<SavingsAccountEntity> accounts;
    private int signedDocumentId;
    private boolean showSignedDocument;
    private String signedDate;
    private double interestRate;
    private String region;

    public List<SavingsAccountEntity> getAccounts() {
        return accounts;
    }
}
