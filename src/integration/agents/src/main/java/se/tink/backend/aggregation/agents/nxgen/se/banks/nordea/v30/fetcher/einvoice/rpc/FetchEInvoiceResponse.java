package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.EInvoiceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchEInvoiceResponse {
    @JsonProperty("results")
    private List<EInvoiceEntity> eInvoices;

    public List<EInvoiceEntity> getEInvoices() {
        return eInvoices;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setEInvoices(List<EInvoiceEntity> eInvoices) {
        this.eInvoices = eInvoices;
    }
}
