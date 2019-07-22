package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String cashAccountType;
    private String bban;

    private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private AccountLinksEntity links;

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(
                        SparebankConstants.ACCOUNT_TYPE_MAPPER
                                .translate(cashAccountType)
                                .orElse(TransactionalAccountType.OTHER))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(bban)
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(Optional.ofNullable(name).orElse(""))
                                .addIdentifier(getIdentifier())
                                .addIdentifier(AccountIdentifier.create(Type.NO, bban))
                                .build())
                .withBalance(BalanceModule.of(getBalance()))
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .addHolderName(Optional.ofNullable(name).orElse(""))
                .putInTemporaryStorage(
                        SparebankConstants.StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .build();
    }

    protected Amount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(this::doesMatchWithAccountCurrency)
                .findFirst()
                .map(balance -> balance.toAmount())
                .orElse(getDefaultAmount());
    }

    protected String getUniqueIdentifier() {
        return iban;
    }

    protected String getAccountNumber() {
        return iban;
    }

    protected AccountIdentifier getIdentifier() {
        return new IbanIdentifier(iban);
    }

    protected List<BalanceEntity> getBalances() {
        return balances;
    }

    protected boolean doesMatchWithAccountCurrency(final BalanceEntity balance) {
        return !balance.isClosingBooked() && balance.isInCurrency(currency);
    }

    protected Amount getDefaultAmount() {
        return new Amount(currency, 0);
    }

    protected String getTransactionLink() {
        return Optional.ofNullable(links).map(AccountLinksEntity::getTransactionLink).orElse("");
    }
}
