package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.EInvoice;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EInvoicesGrouped {
    private String type;
    private String name;
    private String number;
    @JsonProperty("eInvoiceDTO")
    private List<EInvoice> eInvoices;
    private boolean mapped;
    private boolean empty;

    public List<EInvoice> getEInvoices() {
        return eInvoices;
    }
}
