package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionLinksEntity {

    private String next;

    public boolean hasNextLink() {
        return Optional.ofNullable(next).isPresent();
    }

    public String getNextLink() {
        return next;
    }
}
