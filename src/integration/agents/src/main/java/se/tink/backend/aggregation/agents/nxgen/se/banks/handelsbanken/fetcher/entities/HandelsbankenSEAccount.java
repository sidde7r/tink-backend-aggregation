package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities.CardInvoiceInfo;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.validators.BankIdValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountInfoResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class HandelsbankenSEAccount extends HandelsbankenAccount {
    private static final Logger LOG = LoggerFactory.getLogger(HandelsbankenSEAccount.class);

    private HandelsbankenAmount amountAvailable;
    private HandelsbankenAmount balance;
    private String clearingNumber;
    private String number;
    private String numberFormatted;
    private String name;
    private boolean displayBalance;
    private String holderName;
    private boolean isCard;

    public Optional<TransactionalAccount> toTransactionalAccount(
            HandelsbankenApiClient client, TransactionsSEResponse transactionsResponse) {
        if (isCreditCard()) {
            return Optional.empty();
        }

        BankIdValidator.validate(number);

        final String accountNumber = getAccountNumber(transactionsResponse);
        AccountTypes accountType = getAccountType(client, transactionsResponse);

        return Optional.of(
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.from(accountType))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(number)
                                        .withAccountNumber(accountNumber)
                                        .withAccountName(name)
                                        .addIdentifier(new SwedishIdentifier(accountNumber))
                                        .addIdentifier(new SwedishSHBInternalIdentifier(number))
                                        .build())
                        .withBalance(BalanceModule.of(findBalanceAmount().asAmount()))
                        .addHolderName(holderName)
                        .setApiIdentifier(number)
                        .setBankIdentifier(number)
                        .build());
    }

    public Optional<CreditCardAccount> toCreditCardAccount(
            TransactionsSEResponse transactionsResponse) {
        if (!isCreditCard()) {
            return Optional.empty();
        }

        BankIdValidator.validate(number);

        final String accountNumber = getAccountNumber(transactionsResponse);

        CardInvoiceInfo cardInvoiceInfo = transactionsResponse.getCardInvoiceInfo();

        return Optional.of(
                CreditCardAccount.builder(number)
                        .setAvailableCredit(cardInvoiceInfo.getCredit().asAmount())
                        .setHolderName(new HolderName(holderName))
                        .setBalance(
                                new Amount(
                                        balance.getCurrency(),
                                        calculateBalance(transactionsResponse)))
                        .setBankIdentifier(number)
                        .setAccountNumber(accountNumber)
                        .setName(name)
                        .addIdentifier(new SwedishIdentifier(accountNumber))
                        .addIdentifier(new SwedishSHBInternalIdentifier(number))
                        .build());
    }

    private String getAccountNumber(TransactionsSEResponse transactionsResponse) {
        // Clearing number is not set in the account listing, only in the account we receive when we
        // fetch
        // transactions.
        HandelsbankenSEAccount transactionsAccount = transactionsResponse.getAccount();
        return transactionsAccount.getClearingNumber() + "-" + numberFormatted;
    }

    private AccountTypes getAccountType(
            HandelsbankenApiClient client, TransactionsSEResponse transactionsResponse) {
        String accountTypeName = "";
        AccountInfoResponse accountInfo = null;

        try {
            Optional<URL> accountInfoURL = transactionsResponse.getAccount().getAccountInfoUrl();
            if (accountInfoURL.isPresent()) {
                accountInfo = client.accountInfo(accountInfoURL.get());

                accountTypeName =
                        accountInfo
                                .getValuesByLabel()
                                .getOrDefault(
                                        HandelsbankenSEConstants.Accounts.ACCOUNT_TYPE_NAME_LABEL,
                                        name);
            }
        } catch (Exception e) {
            LOG.info("Unable to fetch account info " + e.getMessage());
        }

        AccountTypes accountType =
                HandelsbankenSEConstants.Accounts.ACCOUNT_TYPE_MAPPER
                        .translate(Strings.nullToEmpty(accountTypeName).toLowerCase())
                        .orElse(AccountTypes.OTHER);
        // log unknown account types
        if (accountType == AccountTypes.OTHER && accountInfo != null) {
            LOG.info(
                    String.format(
                            "%s %s",
                            HandelsbankenSEConstants.Accounts.UNKNOWN_ACCOUNT_TYPE,
                            SerializationUtils.serializeToString(accountInfo)));
        }

        if (accountType == AccountTypes.OTHER) {
            accountType =
                    HandelsbankenSEConstants.Accounts.ACCOUNT_TYPE_MAPPER
                            .translate(Strings.nullToEmpty(name).toLowerCase())
                            .orElse(AccountTypes.OTHER);
        }

        return accountType;
    }

    /**
     * 1. Some account have credit some does not. If there is credit, subtract that from
     * amountAvailable (disponibeltbelopp). 2. If no credit but an available amount, use that. 3.
     * Default to balance which always exists.
     */
    private double calculateBalance(TransactionsSEResponse transactionsResponse) {
        CardInvoiceInfo cardInvoiceInfo = transactionsResponse.getCardInvoiceInfo();

        if (cardInvoiceInfo != null
                && cardInvoiceInfo.getCredit() != null
                && this.amountAvailable != null) {
            return this.amountAvailable.getAmount()
                    - cardInvoiceInfo.getCredit().getAmountFormatted();
        } else if (this.amountAvailable != null) {
            return this.amountAvailable.getAmount();
        }
        return this.balance.getAmount();
    }

    private HandelsbankenAmount findBalanceAmount() {
        if (amountAvailable != null) {
            return amountAvailable;
        }
        return balance;
    }

    @Override
    public boolean is(Account account) {
        return number != null && number.equals(account.getBankIdentifier());
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public void applyTo(Transfer transfer) {
        SwedishSHBInternalIdentifier identifier = new SwedishSHBInternalIdentifier(number);
        if (identifier.isValid()) {
            transfer.setSource(identifier);
        }
    }

    @VisibleForTesting
    HandelsbankenSEAccount setNumber(String number) {
        this.number = number;
        return this;
    }

    @VisibleForTesting
    HandelsbankenSEAccount setNumberFormatted(String numberFormatted) {
        this.numberFormatted = numberFormatted;
        return this;
    }

    @VisibleForTesting
    HandelsbankenSEAccount setAmountAvailable(HandelsbankenAmount amountAvailable) {
        this.amountAvailable = amountAvailable;
        return this;
    }

    @JsonIgnore
    public Optional<URL> getAccountInfoUrl() {
        try {
            return Optional.of(findLink(HandelsbankenConstants.URLS.Links.ACCOUNT_INFO));
        } catch (Exception e) {
            LOG.info("Failed to find link for account info ", e.getMessage());
        }

        return Optional.empty();
    }
}
