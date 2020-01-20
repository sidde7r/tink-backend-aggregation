package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities.CardInvoiceInfo;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.validators.BankIdValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.libraries.amount.Amount;
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
        final String accountTypeName = getAccountTypeName(client, transactionsResponse);

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        HandelsbankenSEConstants.Accounts.ACCOUNT_TYPE_MAPPER, accountTypeName)
                .withBalance(BalanceModule.of(findBalanceAmount().asAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(number)
                                .withAccountNumber(accountNumber)
                                .withAccountName(name)
                                .addIdentifier(new SwedishIdentifier(accountNumber))
                                .addIdentifier(new SwedishSHBInternalIdentifier(number))
                                .build())
                .addHolderName(holderName)
                .setApiIdentifier(number)
                .setBankIdentifier(number)
                .build();
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
                        .setAvailableCredit(getAvailableCredit(cardInvoiceInfo))
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

    /**
     * Spendable and amountAvailable should always be the same value. Trying to use any of those as
     * available credit. Doesn't seem likely that they are ever null, but you never know with
     * Handelsbanken, so defaulting to credit limit (which is what we used before).
     */
    private Amount getAvailableCredit(CardInvoiceInfo cardInvoiceInfo) {
        if (cardInvoiceInfo.getSpendable() != null) {
            return cardInvoiceInfo.getSpendable().asAmount();
        }

        if (amountAvailable != null) {
            return amountAvailable.asAmount();
        }

        LOG.warn(
                "No spendable or amountAvailable value found for Handelsbanken credit card: {}",
                name);
        return cardInvoiceInfo.getCredit().asAmount();
    }

    private String getAccountNumber(TransactionsSEResponse transactionsResponse) {
        // Clearing number is not set in the account listing, only in the account we receive when we
        // fetch
        // transactions.
        HandelsbankenSEAccount transactionsAccount = transactionsResponse.getAccount();
        return transactionsAccount.getClearingNumber() + "-" + numberFormatted;
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
                    .getOrDefault(HandelsbankenSEConstants.Accounts.ACCOUNT_TYPE_NAME_LABEL, name);
        }
        return name;
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
        return number != null && number.equals(account.getApiIdentifier());
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
            LOG.info("Failed to find link for account info ", e.getMessage(), e);
        }

        return Optional.empty();
    }
}
