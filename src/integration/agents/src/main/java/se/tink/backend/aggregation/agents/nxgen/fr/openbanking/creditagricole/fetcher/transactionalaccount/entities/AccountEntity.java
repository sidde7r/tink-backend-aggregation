package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    @JsonProperty("_links")
    private LinksEntity links;

    private AccountIdEntity accountId;
    private List<BalanceEntity> balances;
    private String bicFi;
    private String cashAccountType;
    private String currency;
    private String details;
    private String linkedAccount;
    private String name;
    private String product;
    private String psuStatus;
    private String resourceId;
    private String usage;

    public TransactionalAccount toTinkAccount() {
        final TransactionalAccountType transactionalAccountType =
                CreditAgricoleConstants.ACCOUNT_TYPE_MAPPER
                        .translate(cashAccountType)
                        .orElse(TransactionalAccountType.OTHER);
        final String iban = Optional.ofNullable(accountId.getIban()).orElse(resourceId);

        return TransactionalAccount.nxBuilder()
                .withType(transactionalAccountType)
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    private Amount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailablebalance)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.INVALID_BALANCE_TYPE))
                .toAmount();
    }
}
