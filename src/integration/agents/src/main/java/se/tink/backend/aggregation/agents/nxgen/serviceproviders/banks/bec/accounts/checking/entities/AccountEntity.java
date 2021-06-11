package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities;

import static se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType.*;

import java.util.Optional;
import lombok.Getter;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AccountEntity {
    private String accountId;
    private String accountName;
    private double balance;
    private String balanceTxt;
    private String dateLastRecord;
    private double maximum;
    private String maximumTxt;
    private String currency;
    private int accountAuthCode;
    private boolean isStdFraKonto;
    private boolean hasExpenditureOverview;

    public ExactCurrencyAmount getTinkBalance() {
        return ExactCurrencyAmount.of(balance, currency);
    }

    public Optional<TransactionalAccount> toTinkTransactionalAccount(
            AccountDetailsResponse details) {
        // Should not be able to throw an exception here.
        AccountTypes accountType =
                details.getTinkAccountType()
                        .orElseThrow(() -> new IllegalStateException("Unknown account type"));

        TransactionalAccountType transactionalAccountType = from(accountType).orElse(null);

        return TransactionalAccount.nxBuilder()
                .withType(transactionalAccountType)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getTinkBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountId)
                                .withAccountNumber(accountId)
                                .withAccountName(accountName)
                                .addIdentifier(new IbanIdentifier(details.getIban()))
                                .build())
                .addHolderName(details.getAccountHolder())
                .build();
    }
}
