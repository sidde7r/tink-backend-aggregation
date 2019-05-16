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

        return balances.stream()
                .filter(BalanceEntity::isForwardBalanceAvailable)
                .findAny()
                .map(balanceEntity -> balanceEntity.getBalanceAmount().toTinkAmount())
                .orElseGet(this::getInterimBalance);
    }

    private Amount getInterimBalance() {
        return balances.stream()
                .filter(BalanceEntity::isInterimBalanceAvailable)
                .findAny()
                .map(balanceEntity -> balanceEntity.getBalanceAmount().toTinkAmount())
                .orElse(new Amount(FinecoBankConstants.Formats.CURRENCY, 0));
    }
}
