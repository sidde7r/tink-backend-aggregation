package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class QuickbalanceSubscriptionEntity {
    private String id;
    private boolean active;
    private LinksEntity links;
}
