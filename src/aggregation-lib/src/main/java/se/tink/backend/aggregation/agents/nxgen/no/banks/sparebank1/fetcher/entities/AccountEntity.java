package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.backend.utils.StringUtils;

@JsonObject
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

    @JsonIgnore
    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(getTinkAccountType(), formattedNumber, constructAmount())
                .setName(name)
                .setUniqueIdentifier(id)
                .addToTemporaryStorage(Sparebank1Constants.Keys.TRANSACTIONS_LINK,
                        links.get(Sparebank1Constants.Keys.TRANSACTIONS_KEY))
                .build();
    }

    @JsonIgnore
    private Amount constructAmount() {
        return Amount.inNOK(StringUtils.parseAmount(disposableAmountInteger + "," + disposableAmountFraction));
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        switch (accountType.toLowerCase()) {
        case Sparebank1Constants.AccountTypes.CURRENT_ACCOUNT:
        case Sparebank1Constants.AccountTypes.DISPOSABLE_ACCOUNT:
            return AccountTypes.CHECKING;
        case Sparebank1Constants.AccountTypes.SAVINGS_ACCOUNT:
            return AccountTypes.SAVINGS;
        default:
            log.info(String.format("%s: %s (%s)",
                    Sparebank1Constants.Tags.UNKNOWN_ACCOUNT_TYPE, accountType, name));
            return AccountTypes.CHECKING;
        }
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getBalanceAmountFraction() {
        return balanceAmountFraction;
    }

    public String getBalanceAmountInteger() {
        return balanceAmountInteger;
    }

    public Boolean getBalancePreferred() {
        return balancePreferred;
    }

    public String getDisposableAmountFraction() {
        return disposableAmountFraction;
    }

    public String getDisposableAmountInteger() {
        return disposableAmountInteger;
    }

    public String getFormattedNumber() {
        return formattedNumber;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getPaymentFromEnabled() {
        return paymentFromEnabled;
    }

    public Boolean getTransferFromEnabled() {
        return transferFromEnabled;
    }

    public Boolean getTransferToEnabled() {
        return transferToEnabled;
    }
}
