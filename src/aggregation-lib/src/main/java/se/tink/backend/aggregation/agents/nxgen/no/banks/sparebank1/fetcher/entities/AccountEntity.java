package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
    private static final AggregationLogger log = new AggregationLogger(AccountEntity.class);

    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;
    private String accountType;
    private String balanceAmountFraction;
    private String balanceAmountInteger;
    private Boolean balancePreferred;
    private String disposableAmountFraction;
    private String disposableAmountInteger;
    private String formattedNumber;
    private String id;
    private String name;
    private Boolean paymentFromEnabled;
    private Boolean transferFromEnabled;
    private Boolean transferToEnabled;

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public void setLinks(
            HashMap<String, LinkEntity> links) {
        this.links = links;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getBalanceAmountFraction() {
        return balanceAmountFraction;
    }

    public void setBalanceAmountFraction(String balanceAmountFraction) {
        this.balanceAmountFraction = balanceAmountFraction;
    }

    public String getBalanceAmountInteger() {
        return balanceAmountInteger;
    }

    public void setBalanceAmountInteger(String balanceAmountInteger) {
        this.balanceAmountInteger = balanceAmountInteger;
    }

    public Boolean getBalancePreferred() {
        return balancePreferred;
    }

    public void setBalancePreferred(Boolean balancePreferred) {
        this.balancePreferred = balancePreferred;
    }

    public String getDisposableAmountFraction() {
        return disposableAmountFraction;
    }

    public void setDisposableAmountFraction(String disposableAmountFraction) {
        this.disposableAmountFraction = disposableAmountFraction;
    }

    public String getDisposableAmountInteger() {
        return disposableAmountInteger;
    }

    public void setDisposableAmountInteger(String disposableAmountInteger) {
        this.disposableAmountInteger = disposableAmountInteger;
    }

    public String getFormattedNumber() {
        return formattedNumber;
    }

    public void setFormattedNumber(String formattedNumber) {
        this.formattedNumber = formattedNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getPaymentFromEnabled() {
        return paymentFromEnabled;
    }

    public void setPaymentFromEnabled(Boolean paymentFromEnabled) {
        this.paymentFromEnabled = paymentFromEnabled;
    }

    public Boolean getTransferFromEnabled() {
        return transferFromEnabled;
    }

    public void setTransferFromEnabled(Boolean transferFromEnabled) {
        this.transferFromEnabled = transferFromEnabled;
    }

    public Boolean getTransferToEnabled() {
        return transferToEnabled;
    }

    public void setTransferToEnabled(Boolean transferToEnabled) {
        this.transferToEnabled = transferToEnabled;
    }

    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(getTinkAccountType(), formattedNumber, constructAmount())
                .setName(name)
                .setUniqueIdentifier(id)
                .build();
    }

    private Amount constructAmount() {
        return Amount.inNOK(StringUtils.parseAmount(disposableAmountInteger + "," + disposableAmountFraction));
    }

    private AccountTypes getTinkAccountType() {
        switch (accountType.toLowerCase()) {
        case Sparebank1Constants.CURRENT_ACCOUNT:
        case Sparebank1Constants.DISPOSABLE_ACCOUNT:
            return AccountTypes.CHECKING;
        case Sparebank1Constants.SAVINGS_ACCOUNT:
            return AccountTypes.SAVINGS;
        default:
            log.info(String.format("%s: %s (%s)",
                    Sparebank1Constants.LOG_UNKNOWN_ACCOUNT_TYPE_TAG, accountType, name));
            return AccountTypes.CHECKING;
        }
    }
}
