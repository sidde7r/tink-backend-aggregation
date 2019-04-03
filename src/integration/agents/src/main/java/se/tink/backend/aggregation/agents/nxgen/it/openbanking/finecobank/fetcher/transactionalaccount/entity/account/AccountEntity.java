package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.account;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.common.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String iban;
    private String currency;
    private String product;
    private List<BalanceEntity> balances;
    private LinksEntity links;

    public CheckingAccount toTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(getBalance())
                .setAlias(product)
                .addAccountIdentifier(new IbanIdentifier(iban))
                .setApiIdentifier(resourceId)
                .build();
    }

    private Amount getBalance() {

        BalanceEntity balance =
                balances.stream()
                        .filter(BalanceEntity::isForwardBalanceAvailable)
                        .findAny()
                        .orElseGet(
                                () ->
                                        balances.stream()
                                                .filter(BalanceEntity::isInterimBalanceAvailable)
                                                .findAny()
                                                .orElse(null));

        return balance != null
                ? balance.getBalanceAmount().toTinkAmount()
                : new Amount(FinecoBankConstants.Formats.CURRENCY, 0);
    }
}
