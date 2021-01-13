package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.entities.CardTransactionsGroupedEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CardTransactionsResponse {

    private CardTransactionsGroupedEntity cardTransactions;
}
