package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReferenceAccountEntity {
    private String accountOwnerName;
    private String bankCode;
    private String bic;
    private Object currency;
    private String externalAccountNumber;
    private String iban;
    private String linkedAccount;
    private Object technicalAccountNumber;
    private Object productBranch;
    private Object referenceAccountString;

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

    public Object getCurrency() {
        return currency;
    }

    public Object getTechnicalAccountNumber() {
        return technicalAccountNumber;
    }

    public Object getProductBranch() {
        return productBranch;
    }

    public Object getReferenceAccountString() {
        return referenceAccountString;
    }
}
