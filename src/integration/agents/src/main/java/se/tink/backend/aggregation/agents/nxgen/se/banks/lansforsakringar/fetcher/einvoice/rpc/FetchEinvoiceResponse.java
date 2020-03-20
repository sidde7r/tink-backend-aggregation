package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.entities.ResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchEinvoiceResponse {
    // `id` is null - cannot define it!
    private ResponseEntity response;
    // `errors` is null - cannot define it!

    public ResponseEntity getResponse() {
        return response;
    }
}
