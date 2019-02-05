package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InternationalRecipientsEntity {
    private List<RecipientEntity> recipients;

    public List<RecipientEntity> getRecipients() {
        return recipients;
    }
}
