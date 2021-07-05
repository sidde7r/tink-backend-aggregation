package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.BalanceAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.BookedEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BalancesResponse {
    @Getter AccountEntity account;
    List<BalanceEntity> balances;
    @Getter BookedEntity booked;
    LinksEntity links;

    public ExactCurrencyAmount getBalance() {
        return balances.stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::getBalanceAmountEntity)
                .map(BalanceAmountEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException("No balance found in the response"));
    }
}
