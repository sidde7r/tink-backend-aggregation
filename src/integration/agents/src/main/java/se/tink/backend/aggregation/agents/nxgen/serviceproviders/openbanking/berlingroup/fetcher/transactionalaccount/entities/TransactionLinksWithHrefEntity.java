package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionLinksWithHrefEntity {

    private Href next;

    public boolean hasNextLink() {
        return Optional.ofNullable(next).map(Href::getHref).isPresent();
    }

    public String getNextLink() {
        return next.getHref();
    }
}
