package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountTypeEntity {
    private String accountType;
    private String categoryCode;
    private String productCode;
    private boolean longTermSavings;

    public String getAccountType() {
        return accountType;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public boolean isLongTermSavings() {
        return longTermSavings;
    }
}
