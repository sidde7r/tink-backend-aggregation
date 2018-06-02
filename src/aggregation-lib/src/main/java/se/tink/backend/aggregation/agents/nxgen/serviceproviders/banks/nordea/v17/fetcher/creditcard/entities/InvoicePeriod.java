package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvoicePeriod {
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String invoicePeriod;

    public String getInvoicePeriod() {
        return Strings.nullToEmpty(invoicePeriod);
    }
}
