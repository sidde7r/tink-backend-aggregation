package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
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
