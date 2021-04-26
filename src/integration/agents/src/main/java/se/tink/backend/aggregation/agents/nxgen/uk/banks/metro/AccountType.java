package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro;

import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.common.model.EntityType;

public enum AccountType {
    PERSONAL(EntityType.RETAIL),
    BUSINESS(EntityType.BUSINESS);

    private final EntityType entityType;

    AccountType(EntityType entityType) {
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
