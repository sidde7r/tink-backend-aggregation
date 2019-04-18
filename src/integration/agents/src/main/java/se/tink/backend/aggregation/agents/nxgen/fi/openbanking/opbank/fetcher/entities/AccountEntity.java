package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import net.minidev.json.annotate.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    private String accountId;
    private String productName;
    private String identifierSchema;
    private String identifier;
    private String servicerSchema;
    private String servicer;
    private String owner;
    private double netBalance;
    private String grossBalance;
    private String coverReservationAmount;
    private String currency;

    public String getAccountId() {
        return accountId;
    }

    public String getProductName() {
        return productName;
    }

    public String getIdentifierSchema() {
        return identifierSchema;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getServicerSchema() {
        return servicerSchema;
    }

    public String getServicer() {
        return servicer;
    }

    public String getOwner() {
        return owner;
    }

    public double getNetBalance() {
        return netBalance;
    }

    public String getGrossBalance() {
        return grossBalance;
    }

    public String getCoverReservationAmount() {
        return coverReservationAmount;
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(getIdentifier())
                .setAccountNumber(getIdentifier())
                .setBalance(new Amount(getCurrency(), getNetBalance()))
                .setAlias(getProductName())
                //TODO check the identifier scheme
                .addAccountIdentifier(new IbanIdentifier(getIdentifier()))
                .putInTemporaryStorage(OpBankConstants.StorageKeys.ACCOUNT_ID, getAccountId())
                .build();
    }
}
