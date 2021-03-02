package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.FinnishIdentifier;

public class HandelsbankenFIAccount extends HandelsbankenAccount {

    private HandelsbankenAmount balance;
    private HandelsbankenAmount amount;
    private String displayName;
    private String number;
    private String unformattedNumber;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(chooseAmountField().toExactCurrencyAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(unformattedNumber)
                                .withAccountNumber(number)
                                .withAccountName(displayName)
                                .addIdentifier(new FinnishIdentifier(unformattedNumber))
                                .build())
                .setBankIdentifier(unformattedNumber)
                .build();
    }

    private HandelsbankenAmount chooseAmountField() {
        return amount != null ? amount : balance;
    }

    @Override
    public boolean is(Account account) {
        return account.isUniqueIdentifierEqual(unformattedNumber);
    }
}
