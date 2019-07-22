package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateAuthorisationLinksEntity {
    private HrefEntity updatePsuAuthentication;
    private HrefEntity scaStatus;

    public HrefEntity getUpdatePsuAuthentication() {
        return updatePsuAuthentication;
    }
}
