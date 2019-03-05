package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FamilyItemEntity {
    private List<BalancesItemEntity> balances;
    private String name;
    private String id;
}
