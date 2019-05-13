package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.account;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String iban;
    private String bban;
    private String msisdn;
    private String currency;
    private String name;
    private String product;
    private String cashAccountType;
    private String status;
    private String bic;
    private String linkedAccounts;
    private String usage;
    private String details;
    private List<BalanceEntity> balances = null;
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

        return balance != null ? balance.getBalanceAmount().toTinkAmount() : new Amount("EUR", 0);
    }
}
