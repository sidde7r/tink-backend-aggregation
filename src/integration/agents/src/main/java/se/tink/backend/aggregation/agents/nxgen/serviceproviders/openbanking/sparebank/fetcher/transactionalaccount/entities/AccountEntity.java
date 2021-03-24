package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    @Getter private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String cashAccountType;
    private String bban;
    private String product;

    @JsonProperty("_links")
    private AccountLinksEntity links;

    public Optional<TransactionalAccount> toTinkAccount(BalanceResponse balancesResponse) {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        SparebankConstants.ACCOUNT_TYPE_MAPPER,
                        Optional.ofNullable(cashAccountType).orElse(product),
                        TransactionalAccountType.OTHER)
                .withBalance(BalanceModule.of(getBalance(balancesResponse)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(Optional.ofNullable(name).orElse(""))
                                .addIdentifier(getIdentifier())
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.NO, bban))
                                .build())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .build();
    }

    protected ExactCurrencyAmount getBalance(BalanceResponse balancesResponse) {
        return Optional.ofNullable(balancesResponse.getBalances()).orElse(Collections.emptyList())
                .stream()
                .filter(this::doesMatchWithAccountCurrency)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SparebankConstants.ErrorMessages.NO_AMOUNT_FOUND));
    }

    private String getUniqueIdentifier() {
        return bban;
    }

    private String getAccountNumber() {
        return iban;
    }

    private AccountIdentifier getIdentifier() {
        return new IbanIdentifier(iban);
    }

    private boolean doesMatchWithAccountCurrency(final BalanceEntity balance) {
        return !balance.isClosingBooked() && balance.isInCurrency(currency);
    }

    protected String getTransactionLink() {
        return Optional.ofNullable(links).map(AccountLinksEntity::getTransactionLink).orElse("");
    }
}
