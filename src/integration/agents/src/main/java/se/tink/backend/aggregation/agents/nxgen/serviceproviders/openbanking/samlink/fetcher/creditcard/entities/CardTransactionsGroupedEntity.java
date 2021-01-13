package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.entities;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CardTransactionsGroupedEntity {
    private List<CardTransactionEntity> booked;
    private List<CardTransactionEntity> pending;
}
