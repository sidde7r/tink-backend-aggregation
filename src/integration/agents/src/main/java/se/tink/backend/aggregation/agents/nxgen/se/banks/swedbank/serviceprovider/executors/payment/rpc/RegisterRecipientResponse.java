package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterRecipientResponse {
    private LinksEntity links;
    private String name;
    private String accountNumber;

    public LinksEntity getLinks() {
        return links;
    }
}
