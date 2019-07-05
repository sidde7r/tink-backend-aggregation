package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvoiceEntity {
    @JsonProperty("EncryptedInvoicedId")
    private String encryptedInvoicedId;

    @JsonProperty("IsAmountChangeable")
    private boolean isAmountChangeable;
}
