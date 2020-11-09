package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionEntity {

    private List<TransactionDetailsEntity> booked = Collections.emptyList();
    private List<TransactionDetailsEntity> pending = Collections.emptyList();
}
