package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuppressWarnings("unused")
public class SecurityKeyIndexEntity {
    private CardEntity card;
    private String index;

    public String getIndex() {
        return index;
    }
}

@JsonObject
@SuppressWarnings("unused")
class CardEntity {
    private DetailsEntity details;
    private String id;
}

@JsonObject
@SuppressWarnings("unused")
class DetailsEntity {
    private boolean fixed;
    private int keyCount;
    private boolean keysUsed;
    private String nextSecurityCardIndex;
}
