package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.entities.transactions;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionsEntity {

    private List<TransactionsItemEntity> booked;
    private List<TransactionsItemEntity> pending;
}
