package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CardTransactionsEntity {

    private List<CardTransactionEntity> booked;
    private List<CardTransactionEntity> pending;

    @JsonProperty("_links")
    private LinksEntity links;

    public List<CardTransactionEntity> getBooked() {
        return booked == null ? Collections.emptyList() : booked;
    }

    public List<CardTransactionEntity> getPending() {
        return pending == null ? Collections.emptyList() : pending;
    }
}
