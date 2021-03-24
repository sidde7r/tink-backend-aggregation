package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.AccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    private String id;
    private String accountNumber;
    private String label;
    private String holder;
    private String devise;
    private double balance;
    private String balanceDate;
    private boolean ribEnable;
    private boolean savingEnable;
    private boolean isInBudget;
    private boolean isInPortfolio;
    private boolean isPersonnal;
    private boolean isGhost;
    private boolean visible;
    private boolean selected;
    private String color;
    private String productCategory;
    private String productType;
    private String productFamily;
    private String contractFamily;
    private List<CreditCardsEntity> creditCards;
    private String subAccountNumber;
    private String status;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(AccountType.ACCOUNT_TYPE_MAPPER, productType)
                .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(balance)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(productType + " " + label)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.IBAN, accountNumber))
                                .build())
                .addHolderName(holder)
                .setApiIdentifier(id)
                .build();
    }

    public boolean isKnownAccountType() {
        return AccountType.CHECKING.equalsIgnoreCase(productType)
                || AccountType.SAVINGS.equalsIgnoreCase(productType);
    }

    private AccountTypes getTinkAccountType() {
        if (AccountType.CHECKING.equalsIgnoreCase(productType)) {
            return AccountTypes.CHECKING;
        } else if (AccountType.SAVINGS.equalsIgnoreCase(productType)) {
            return AccountTypes.SAVINGS;
        }
        return AccountTypes.OTHER;
    }
}
