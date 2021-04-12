package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants.Accounts;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountInfoResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class HandelsbankenSEAccount extends HandelsbankenAccount {
    private static final Logger LOG = LoggerFactory.getLogger(HandelsbankenSEAccount.class);
    private String accountName; // e.g. "Affärskonto"
    private String accountNo; // e.g. "123456789" (does not include clearing number)
    private String accountNoFormatted; // e.g. "123 456 789
    private BigDecimal approvedCredit; // e.g. 0
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

        final AccountInfoResponse accountInfo =
                transactionsResponse
                        .getAccount()
                        .getAccountInfoUrl()
                        .map(url -> client.accountInfo(url))
                        .orElse(null);
        if (Objects.isNull(accountInfo)) {
            LOG.warn("Did not get account info, ignoring account");
            return Optional.empty();
        }

        final String accountNumber =
                accountInfo.getValuesByLabel().get(Accounts.ACCOUNT_NUMBER_LABEL);
        if (Strings.isNullOrEmpty(accountNumber)) {
            LOG.warn("Did not get full account number, ignoring account");
            return Optional.empty();
        }

        final String accountTypeName = getAccountTypeName(accountInfo);

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(Accounts.ACCOUNT_TYPE_MAPPER, accountTypeName)
                .withBalance(getBalance())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNo)
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountName)
                                .addIdentifiers(getIdentifiers(accountInfo))
                                .build())
                .setApiIdentifier(accountNo)
                .setBankIdentifier(accountNo)
                .sourceInfo(createAccountSourceInfo())
                .build();
    }

    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder().bankProductName(accountName).build();
    }

    private BalanceModule getBalance() {
        return BalanceModule.builder()
                .withBalance(ExactCurrencyAmount.of(findBalanceAmount(), currencyCode))
                .setAvailableBalance(
                        ExactCurrencyAmount.of(
                                findBalanceAmount().subtract(approvedCredit), currencyCode))
                .setCreditLimit(ExactCurrencyAmount.of(approvedCredit, currencyCode))
                .build();
    }

    private List<AccountIdentifier> getIdentifiers(AccountInfoResponse accountInfo) {
        final List<AccountIdentifier> identifiers = new LinkedList<>();
        final Map<String, String> accountValues = accountInfo.getValuesByLabel();

        identifiers.add(new SwedishSHBInternalIdentifier(accountNo));
        identifiers.add(new SwedishIdentifier(accountValues.get(Accounts.ACCOUNT_NUMBER_LABEL)));

        if (accountValues.containsKey(Accounts.BANKGIRO_NUMBER_LABEL)) {
            final String bankgiroNumber = accountValues.get(Accounts.BANKGIRO_NUMBER_LABEL);
            identifiers.add(new BankGiroIdentifier(bankgiroNumber));
        }

        if (accountValues.containsKey(Accounts.IBAN_LABEL)) {
            final String iban = accountValues.get(Accounts.IBAN_LABEL).replaceAll("\\s", "");
            identifiers.add(new IbanIdentifier(iban));
        }

        return identifiers;
    }

    /**
     * Account type name is only present if the account name has been changed. Otherwise the account
     * name contains the account type name.
     */
    private String getAccountTypeName(AccountInfoResponse accountInfo) {
        return accountInfo
                .getValuesByLabel()
                .getOrDefault(Accounts.ACCOUNT_TYPE_NAME_LABEL, accountName);
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
