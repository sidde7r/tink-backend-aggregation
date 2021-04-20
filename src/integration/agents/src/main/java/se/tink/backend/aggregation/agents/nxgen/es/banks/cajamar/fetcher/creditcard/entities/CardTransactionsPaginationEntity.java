package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.entities;

import io.vavr.collection.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PaginationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CardTransactionsPaginationEntity {
    private List<CardTransactionDataEntity> dataList;
    private PaginationEntity pagination;
}
