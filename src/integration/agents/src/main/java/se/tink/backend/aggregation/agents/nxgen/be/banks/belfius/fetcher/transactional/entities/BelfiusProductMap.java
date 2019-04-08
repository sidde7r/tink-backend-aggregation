package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Collections;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.serializer.BelfiusProductMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonDeserialize(using = BelfiusProductMapDeserializer.class)
public class BelfiusProductMap {

    private final Map<String, BelfiusProduct> accounts;

    public BelfiusProductMap(Map<String, BelfiusProduct> accounts) {
        this.accounts = accounts;
    }

    public Map<String, BelfiusProduct> getProducts() {
        return this.accounts != null ? this.accounts : Collections.emptyMap();
    }
}
