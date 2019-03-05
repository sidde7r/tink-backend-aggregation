package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesItemEntity {
    private String name;
    private String id;
    private List<AmountBalancesItemEntity> amountBalances;
}
