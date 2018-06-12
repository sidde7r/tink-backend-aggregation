package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;

public abstract class AbstractBankIdSignResponse {
    private String signingStatus;
    private LinksEntity links;

    public String getSigningStatus() {
        return signingStatus;
    }

    public LinksEntity getLinks() {
        return links;
    }
}
