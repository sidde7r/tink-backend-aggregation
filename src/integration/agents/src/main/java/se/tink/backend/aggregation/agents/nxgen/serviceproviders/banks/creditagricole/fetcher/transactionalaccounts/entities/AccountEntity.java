package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.AccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
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
                .withId(getIdModule())
                .addHolderName(getHolderName())
                .setApiIdentifier(id)
                .build();
    }

    public boolean isKnownAccountType() {
        return AccountType.CHECKING.equalsIgnoreCase(productType)
                || AccountType.SAVINGS.equalsIgnoreCase(productType);
    }

    private IdModule getIdModule() {
        String accountNumberValue = Optional.ofNullable(accountNumber).orElse("");
        return IdModule.builder()
                .withUniqueIdentifier(accountNumberValue)
                .withAccountNumber(accountNumberValue)
                .withAccountName(getAccountName())
                .addIdentifier(new IbanIdentifier(accountNumberValue))
                .build();
    }

    private String getHolderName() {
        return Optional.ofNullable(holder)
                .map(String::trim)
                .map(s -> s.replaceAll("\\s+", " "))
                .orElse("");
    }

    private String getAccountName() {
        return String.format("%s %s", productType, label);
    }
}
