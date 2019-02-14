package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserFeatures {
    private int availableSpaces;
    private boolean canUpgrade;
}
