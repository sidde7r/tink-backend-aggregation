package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.account;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.Currency;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
    private List<BalanceEntity> balances;
    private LinksEntity links;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(
                        RaiffeisenConstants.ACCOUNT_TYPE_MAPPER
                                .translate(cashAccountType)
                                .orElse(TransactionalAccountType.CHECKING))
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    private ExactCurrencyAmount getBalance() {
        return balances.stream()
                .filter(BalanceEntity::isForwardBalanceAvailable)
                .findAny()
                .map(balanceEntity -> balanceEntity.getBalanceAmount().toTinkAmount())
                .orElseGet(this::getInterimBalance);
    }

    private ExactCurrencyAmount getInterimBalance() {
        return balances.stream()
                .filter(BalanceEntity::isInterimBalanceAvailable)
                .findAny()
                .map(balanceEntity -> balanceEntity.getBalanceAmount().toTinkAmount())
                .orElse(ExactCurrencyAmount.zero(Currency.EUR));
    }
}
