package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities;

import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.AccountType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.Currency;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;

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

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(
                        getTinkAccounType(), accountNumber, new Amount(Currency.EUR, balance))
                .setAccountNumber(accountNumber)
                .setHolderName(new HolderName(holder))
                .setName(label)
                .build();
    }

    public boolean isKnownAccountType() {
        return productType.equalsIgnoreCase(AccountType.CHECKING);
    }

    private AccountTypes getTinkAccounType() {
        // Currently only has data on checking accounts, so this
        // statement is to show how future implementation could look
        if (productType.equalsIgnoreCase(AccountType.CHECKING)) {
            return AccountTypes.CHECKING;
        }
        return AccountTypes.OTHER;
    }
}
