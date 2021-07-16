package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.entities.ResponseControlEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchEinvoiceRequest {
    private ResponseControlEntity responseControl;
    private String customerId;

    private FetchEinvoiceRequest(ResponseControlEntity responseControl, String customerId) {
        this.responseControl = responseControl;
        this.customerId = customerId;
    }

    @JsonIgnore
    public static FetchEinvoiceRequest of(
            String profileType, String eInvoiceStatus, String customerId) {
        return new FetchEinvoiceRequest(
                ResponseControlEntity.of(profileType, customerId, eInvoiceStatus), customerId);
    }
}
