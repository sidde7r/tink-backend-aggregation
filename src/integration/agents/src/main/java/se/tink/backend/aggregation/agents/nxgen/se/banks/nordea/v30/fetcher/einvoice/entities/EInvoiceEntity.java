package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EInvoiceEntity {
    @JsonProperty("e_invoice_url_external")
    private String eInvoiceUrlExternal;
}
