package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.AccountTypes.CURRENT_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.AccountTypes.DISPOSABLE_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.AccountTypes.SAVINGS_ACCOUNT;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Keys;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Tags;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String id;
    private String name;
    private String formattedNumber;
    private String disposableAmountInteger;
    private String disposableAmountFraction;
    private String balanceAmountInteger;
    private String balanceAmountFraction;
    private String currencyCode;
    private String accountType;
    private String accountNumber;
    private boolean paymentFromEnabled;
    private boolean transferFromEnabled;
    private boolean transferToEnabled;
    private boolean balancePreferred;
    private boolean defaultAccount;

    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    @JsonIgnore
    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(
                        getTinkAccountType(),
                        getTinkFormattedAccountNumber(),
                        Sparebank1AmountUtils.constructAmount(
                                disposableAmountInteger, disposableAmountFraction))
                .setAccountNumber(getTinkFormattedAccountNumber())
                .setName(name)
                .putInTemporaryStorage(Keys.TRANSACTIONS_LINK, links.get(Keys.TRANSACTIONS_KEY))
                .build();
    }

    @JsonIgnore
    private String getTinkFormattedAccountNumber() {
        // Prefer accountNumber as it is unformatted and should only contain digits, always remove
        // all that is
        // not digits just in case they change format going forward.
        String formattedAccountNumber =
                !Strings.isNullOrEmpty(accountNumber) ? accountNumber : formattedNumber;

        return formattedAccountNumber.replaceAll("[^0-9]", "");
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        switch (accountType.toLowerCase()) {
            case CURRENT_ACCOUNT:
            case DISPOSABLE_ACCOUNT:
                return AccountTypes.CHECKING;
            case SAVINGS_ACCOUNT:
                return AccountTypes.SAVINGS;
            default:
                logger.info("{}: {} ({})", Tags.UNKNOWN_ACCOUNT_TYPE, accountType, name);
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
