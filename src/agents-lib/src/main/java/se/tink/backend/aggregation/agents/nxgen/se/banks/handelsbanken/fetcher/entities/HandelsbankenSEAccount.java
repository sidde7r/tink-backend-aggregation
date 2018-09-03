package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import com.google.common.annotations.VisibleForTesting;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.validators.BankIdValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Fetcher.Accounts.NAME_SAVINGS_1;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Fetcher.Accounts.NAME_SAVINGS_2;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants.URLS.Links.CARD_TRANSACTIONS;

public class HandelsbankenSEAccount extends HandelsbankenAccount {

    private HandelsbankenAmount amountAvailable;
    private HandelsbankenAmount balance;
    private String number;
    private String numberFormatted;
    private String name;
    // Possibly interesting fields:
    //private boolean displayBalance;
    //private String holderName;
    //private boolean isCard;

    public TransactionalAccount toTransactionalAccount(
            ApplicationEntryPointResponse applicationEntryPoint) {
        BankIdValidator.validate(number);
        final String accountNumber = applicationEntryPoint.getClearingNumber() + "-" + numberFormatted;

        AccountTypes accountType = AccountTypes.CHECKING;
        if (searchLink(CARD_TRANSACTIONS).isPresent()) {
            accountType = AccountTypes.CREDIT_CARD;
        } else if (NAME_SAVINGS_1.equalsIgnoreCase(name) || NAME_SAVINGS_2.equalsIgnoreCase(name)) {
            accountType = AccountTypes.SAVINGS;
        }

        return TransactionalAccount.builder(accountType, accountNumber, findBalanceAmount().asAmount())
                .setBankIdentifier(number)
                .setAccountNumber(accountNumber)
                .setName(name)
                .addIdentifier(new SwedishIdentifier(accountNumber))
                .addIdentifier(new SwedishSHBInternalIdentifier(number))
                .build();
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
    HandelsbankenSEAccount setAmountAvailable(
            HandelsbankenAmount amountAvailable) {
        this.amountAvailable = amountAvailable;
        return this;
    }
}
