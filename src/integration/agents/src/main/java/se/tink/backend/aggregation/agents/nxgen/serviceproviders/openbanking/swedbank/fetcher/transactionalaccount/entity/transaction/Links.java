package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private Href scaStatus;

    private Href scaRedirect;

    private Href status;

    private Href download;

    public Href getHrefEntity() {
        return scaRedirect;
    }

    public Href getScaStatus() {
        return scaStatus;
    }

    public Href getStatus() {
        return status;
    }

    public Href getDownload() {
        return download;
    }
}
