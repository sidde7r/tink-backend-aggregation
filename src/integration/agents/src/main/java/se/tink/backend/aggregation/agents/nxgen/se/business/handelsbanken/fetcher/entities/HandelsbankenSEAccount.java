package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class HandelsbankenSEAccount extends HandelsbankenAccount {
    private static final Logger LOG = LoggerFactory.getLogger(HandelsbankenSEAccount.class);
    private String accountName; // e.g. "Affärskonto"
    private String accountNo; // e.g. "123456789"
    private String accountNoFormatted; // e.g. "123 456 789
    private int approvedCredit; // e.g. 0
    private String approvedCreditFormatted; // e.g "0,00"
    private BigDecimal availableBalance; // e.g. 123456.78 (seems to be same as currentBalance)
    private String availableBalanceFormatted; // e.g. "123 456,78"
    private String currencyCode; // e.g. "SEK"
    private BigDecimal currentBalance; // e.g. 123456.78 (seems to be same as availableBalance)
    private String currentBalanceFormatted; // e.g. "123 456,78"

    public Optional<TransactionalAccount> toTransactionalAccount(
            HandelsbankenApiClient client, TransactionsSEResponse transactionsResponse) {
        if (isCreditCard()) {
            return Optional.empty();
        }

        final String accountTypeName = getAccountTypeName(client, transactionsResponse);

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        HandelsbankenSEConstants.Accounts.ACCOUNT_TYPE_MAPPER, accountTypeName)
                .withBalance(getBalance())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNo)
                                .withAccountNumber(accountNo)
                                .withAccountName(accountName)
                                .addIdentifier(new SwedishIdentifier(accountNo))
                                .addIdentifier(new SwedishSHBInternalIdentifier(accountNo))
                                .build())
                .setApiIdentifier(accountNo)
                .setBankIdentifier(accountNo)
                .build();
    }

    private BalanceModule getBalance() {
        return BalanceModule.builder()
                .withBalance(ExactCurrencyAmount.of(findBalanceAmount(), currencyCode))
                .build();
    }

    /**
     * Account type name is only present if the account name has been changed. Otherwise the account
     * name contains the account type name.
     */
    private String getAccountTypeName(
            HandelsbankenApiClient client, TransactionsSEResponse transactionsResponse) {
        final Optional<URL> accountInfoURL = transactionsResponse.getAccount().getAccountInfoUrl();

        if (accountInfoURL.isPresent()) {
            return client.accountInfo(accountInfoURL.get())
                    .getValuesByLabel()
                    .getOrDefault(
                            HandelsbankenSEConstants.Accounts.ACCOUNT_TYPE_NAME_LABEL, accountName);
        }
        return accountName;
    }

    private BigDecimal findBalanceAmount() {
        if (availableBalance != null) {
            return availableBalance;
        }
        return currentBalance;
    }

    @Override
    public boolean is(Account account) {
        return accountNo != null && accountNo.equals(account.getApiIdentifier());
    }

    @JsonIgnore
    private Optional<URL> getAccountInfoUrl() {
        try {
            return Optional.of(findLink(HandelsbankenConstants.URLS.Links.ACCOUNT_INFO));
        } catch (Exception e) {
            LOG.info("Failed to find link for account info {}", e.getMessage(), e);
        }

        return Optional.empty();
    }
}
