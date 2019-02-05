package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentIdEntity {
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String isin;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String market;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String currency;

    public String getIsin() {
        return isin;
    }

    public String getMarket() {
        return market;
    }

    public String getCurrency() {
        return currency;
    }
}
