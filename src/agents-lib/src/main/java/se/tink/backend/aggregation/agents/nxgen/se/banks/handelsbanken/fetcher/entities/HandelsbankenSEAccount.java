package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities.CardInvoiceInfo;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.validators.BankIdValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Fetcher.Accounts.NAME_SAVINGS_1;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Fetcher.Accounts.NAME_SAVINGS_2;

public class HandelsbankenSEAccount extends HandelsbankenAccount {

    private HandelsbankenAmount amountAvailable;
    private HandelsbankenAmount balance;
    private String clearingNumber;
    private String number;
    private String numberFormatted;
    private String name;
    private boolean displayBalance;
    private String holderName;
    private boolean isCard;

    public Optional<TransactionalAccount> toTransactionalAccount(TransactionsSEResponse transactionsResponse) {
        if (isCreditCard()) {
            return Optional.empty();
        }

        BankIdValidator.validate(number);

        final String accountNumber = getAccountNumber(transactionsResponse);

        AccountTypes accountType = AccountTypes.CHECKING;
        if (NAME_SAVINGS_1.equalsIgnoreCase(name) || NAME_SAVINGS_2.equalsIgnoreCase(name)) {
            accountType = AccountTypes.SAVINGS;
        }

        return Optional.of(TransactionalAccount.builder(accountType, number, findBalanceAmount().asAmount())
                .setHolderName(new HolderName(holderName))
                .setBankIdentifier(number)
                .setAccountNumber(accountNumber)
                .setName(name)
                .addIdentifier(new SwedishIdentifier(accountNumber))
                .addIdentifier(new SwedishSHBInternalIdentifier(number))
                .build());
    }

    public Optional<CreditCardAccount> toCreditCardAccount(TransactionsSEResponse transactionsResponse) {
        if (!isCreditCard()) {
            return Optional.empty();
        }

        BankIdValidator.validate(number);

        final String accountNumber = getAccountNumber(transactionsResponse);

        CardInvoiceInfo cardInvoiceInfo = transactionsResponse.getCardInvoiceInfo();

        return Optional.of(CreditCardAccount.builder(number)
                .setAvailableCredit(cardInvoiceInfo.getCredit().asAmount())
                .setHolderName(new HolderName(holderName))
                .setBalance(new Amount(balance.getCurrency(), calculateBalance(transactionsResponse)))
                .setBankIdentifier(number)
                .setAccountNumber(accountNumber)
                .setName(name)
                .addIdentifier(new SwedishIdentifier(accountNumber))
                .addIdentifier(new SwedishSHBInternalIdentifier(number))
                .build());
    }

    private String getAccountNumber(TransactionsSEResponse transactionsResponse) {
        // Clearing number is not set in the account listing, only in the account we receive when we fetch
        // transactions.
        HandelsbankenSEAccount transactionsAccount = transactionsResponse.getAccount();
        return transactionsAccount.getClearingNumber() + "-" + numberFormatted;
    }

    /**
     * 1. Some account have credit some does not. If there is credit,
     * subtract that from amountAvailable (disponibeltbelopp).
     * 2. If no credit but an available amount, use that.
     * 3. Default to balance which always exists.
     */
    private double calculateBalance(TransactionsSEResponse transactionsResponse) {
        CardInvoiceInfo cardInvoiceInfo = transactionsResponse.getCardInvoiceInfo();

        if (cardInvoiceInfo != null && cardInvoiceInfo.getCredit() != null && this.amountAvailable != null) {
            return this.amountAvailable.getAmount() - cardInvoiceInfo.getCredit().getAmountFormatted();
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
}
