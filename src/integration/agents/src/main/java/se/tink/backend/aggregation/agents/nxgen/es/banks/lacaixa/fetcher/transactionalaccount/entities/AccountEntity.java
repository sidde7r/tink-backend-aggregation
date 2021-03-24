package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;

@JsonObject
public class AccountEntity {

    private String alias;

    @JsonProperty("tipoCuenta")
    private String accountType;

    @JsonProperty("saldo")
    private BalanceEntity balance;

    @JsonProperty("numeroCuenta")
    private AccountIdentifierEntity identifiers;

    public AccountIdentifierEntity getIdentifiers() {
        return identifiers;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(List<Party> parties) {
        AccountTypes type =
                LaCaixaConstants.ACCOUNT_TYPE_MAPPER
                        .translate(accountType)
                        .orElse(AccountTypes.OTHER);

        if (type == AccountTypes.OTHER) {
            return Optional.empty();
        }

        final AccountIdentifier ibanIdentifier =
                AccountIdentifier.create(AccountIdentifierType.IBAN, identifiers.getIban());
        final DisplayAccountIdentifierFormatter formatter = new DisplayAccountIdentifierFormatter();
        final String formattedIban = ibanIdentifier.getIdentifier(formatter);

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(LaCaixaConstants.ACCOUNT_TYPE_MAPPER, accountType)
                .withBalance(BalanceModule.of(balance.toExactCurrencyAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(identifiers.getIban())
                                .withAccountNumber(formattedIban)
                                .withAccountName(alias)
                                .addIdentifier(ibanIdentifier)
                                .build())
                .addParties(parties)
                .putInTemporaryStorage(
                        LaCaixaConstants.TemporaryStorage.ACCOUNT_REFERENCE,
                        identifiers.getAccountReference())
                .build();
    }
}
