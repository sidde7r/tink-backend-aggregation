package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountDetailsResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private boolean paymentService;

    public boolean isPaymentService() {
        return paymentService;
    }
}
