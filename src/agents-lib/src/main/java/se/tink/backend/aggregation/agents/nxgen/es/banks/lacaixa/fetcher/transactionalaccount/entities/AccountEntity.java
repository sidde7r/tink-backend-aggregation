package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class AccountEntity {
    private static final AggregationLogger LOGGER = new AggregationLogger(AccountEntity.class);

    private String alias;

    @JsonProperty("tipoCuenta")
    private String accountType;

    @JsonProperty("saldo")
    private BalanceEntity balance;

    @JsonProperty("numeroCuenta")
    private AccountIdentifierEntity identifiers;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(HolderName holderName) {
        AccountTypes type = LaCaixaConstants.ACCOUNT_TYPE_MAPPER.translate(accountType).orElse(AccountTypes.OTHER);

        if (type == AccountTypes.OTHER) {
            // log all accounts to try to find out what types we are receiving
            LOGGER.infoExtraLong(SerializationUtils.serializeToString(this),
                    LaCaixaConstants.LogTags.UNKNOWN_ACCOUNT_TYPE);

            return Optional.empty();
        }

        return Optional.of(TransactionalAccount.builder(type, identifiers.getIban(), balance)
                .setAccountNumber(identifiers.getIban())
                .setName(alias)
                .addIdentifiers(identifiers.getIdentifiers())
                .putInTemporaryStorage(LaCaixaConstants.TemporaryStorage.ACCOUNT_REFERENCE,
                        identifiers.getAccountReference())
                .setHolderName(holderName)
                .build());
    }
}
