package se.tink.backend.rpc;

import java.util.List;

import io.protostuff.Tag;

public class GiroLookupResponse {
    @Tag(1)
    private List<GiroLookupEntity> giroEntities;

    public GiroLookupResponse(List<GiroLookupEntity> giroEntities) {
        this.giroEntities = giroEntities;
    }

    public List<GiroLookupEntity> getIdentifiers() {
        return giroEntities;
    }

    public void setIdentifiers(List<GiroLookupEntity> giroEntities) {
        this.giroEntities = giroEntities;
    }
}
