package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.BalanceDataEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.MetaEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CreditBalanceResponse {
    @JsonProperty("Data")
    private BalanceDataEntity data;

    @JsonProperty("Links")
    private LinksEntity links;

    @JsonProperty("Meta")
    private MetaEntity meta;

    // TODO: verify if there can be more than 1 accountId

    private double getTinkBalance(BalanceEntity balance) {
        double availableLimit = Double.parseDouble(balance.getBalanceEntity().getAvailableLimit());
        double creditLimit = Double.parseDouble(balance.getBalanceEntity().getCreditLimit());
        return availableLimit - creditLimit;
    }

    public Amount toTinkBalanceAmount(String accountId) {
        final BalanceEntity balance = getBalance(accountId);

        return new Amount(balance.getBalanceEntity().getCurrency(), getTinkBalance(balance));
    }

    public Amount toTinkAvailableCreditAmount(String accountId) {
        final BalanceEntity balance = getBalance(accountId);

        return new Amount(
                balance.getBalanceEntity().getCurrency(),
                Double.parseDouble(balance.getBalanceEntity().getAvailableLimit()));
    }

    public BalanceEntity getBalance(String accountId) {
        return Optional.ofNullable(data)
                .map(BalanceDataEntity::getBalance)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(balanceEntity -> balanceEntity.getAccountId().equalsIgnoreCase(accountId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_BALANCE));
    }
}
