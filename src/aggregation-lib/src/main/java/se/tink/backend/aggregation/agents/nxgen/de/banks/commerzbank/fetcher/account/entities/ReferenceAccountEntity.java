package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReferenceAccountEntity {
    private String accountOwnerName;
    private String bankCode;
    private String bic;
    // `currency` is null - cannot define it!
    private String externalAccountNumber;
    private String iban;
    private String linkedAccount;

    public String getAccountOwnerName() {
        return accountOwnerName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBic() {
        return bic;
    }

    public String getExternalAccountNumber() {
        return externalAccountNumber;
    }

    public String getIban() {
        return iban;
    }

    public String getLinkedAccount() {
        return linkedAccount;
    }
    // `technicalAccountNumber` is null - cannot define it!
    // `productBranch` is null - cannot define it!
    // `referenceAccountString` is null - cannot define it!
}
