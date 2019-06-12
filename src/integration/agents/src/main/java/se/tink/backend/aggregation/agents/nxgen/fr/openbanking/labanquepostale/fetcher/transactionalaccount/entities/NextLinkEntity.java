package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NextLinkEntity {

    private String href;

    public boolean hasNextLink() {
        return href != null;
    }

    public String getsNextLink() {
        return href;
    }
}
