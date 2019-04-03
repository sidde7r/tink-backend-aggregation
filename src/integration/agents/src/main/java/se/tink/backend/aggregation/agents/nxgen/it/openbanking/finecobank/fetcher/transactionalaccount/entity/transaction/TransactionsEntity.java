package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.transaction;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.common.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {

    private List<BookedEntity> booked;
    private LinksEntity links;

    public List<BookedEntity> getBooked() {
        return booked;
    }
}
