package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import com.google.common.annotations.VisibleForTesting;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.validators.BankIdValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;

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

        return CheckingAccount.builder(accountNumber, findBalanceAmount().asAmount())
                .setName(name)
                .setUniqueIdentifier(number)
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
