package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String alias;

    @JsonProperty("saldoDisponible")
    private double availableBalance;

    @JsonProperty("moneda")
    private String currency;

    @JsonProperty("numeroCuenta")
    private AccountIdentifierEntity identifiers;

    public AccountIdentifierEntity getIdentifiers() {
        return identifiers;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(List<Party> parties) {
        TransactionalAccountType type =
                ImaginBankConstants.ACCOUNT_TYPE_MAPPER
                        .translate(alias)
                        .orElse(TransactionalAccountType.OTHER);

        if (type == TransactionalAccountType.OTHER) {
            return Optional.empty();
        }

        return TransactionalAccount.nxBuilder()
                .withType(type)
                .withInferredAccountFlags()
                .withBalance(
                        BalanceModule.of(
                                new ExactCurrencyAmount(
                                        BigDecimal.valueOf(availableBalance), currency)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(identifiers.getIban())
                                .withAccountNumber(formatIban(identifiers.getIban()))
                                .withAccountName(alias)
                                .addIdentifier(new IbanIdentifier(identifiers.getIban()))
                                .build())
                .setBankIdentifier(identifiers.getIban())
                .addParties(parties)
                .putInTemporaryStorage(
                        ImaginBankConstants.TemporaryStorage.ACCOUNT_REFERENCE,
                        identifiers.getAccountReference())
                .build();
    }

    @JsonIgnore
    private String formatIban(String iban) {
        return new DisplayAccountIdentifierFormatter()
                .apply(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
    }
}
