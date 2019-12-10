package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.IBANPortugal;

public class TransactionalAccountBaseInfo {

    private static final String BANK_ID = "0010";
    private static final String PSP_REFERENCE_NUMBER = "0000";
    private static final String INTERNAL_ACCOUNT_ID_POSTFIX = "00001";

    private String internalAccountId;
    private String type;
    private String order;
    private String accountName;
    private String clientId;
    private String currency;
    private String iban;

    public String getInternalAccountId() {
        return internalAccountId;
    }

    public void setInternalAccountId(String internalAccountId) {
        this.internalAccountId = internalAccountId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getIban() {
        if (iban == null) {
            iban =
                    IBANPortugal.generateIBAN(
                            "0010", "0000", getInternalAccountId() + INTERNAL_ACCOUNT_ID_POSTFIX);
        }
        return iban;
    }
}
