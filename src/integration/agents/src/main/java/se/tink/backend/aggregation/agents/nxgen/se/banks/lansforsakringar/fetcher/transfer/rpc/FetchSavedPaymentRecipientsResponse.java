package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.rpc;

import com.google.api.client.util.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.entities.RecipientsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchSavedPaymentRecipientsResponse {
    private List<RecipientsEntity> recipients;

    public List<RecipientsEntity> getRecipients() {
        return Optional.ofNullable(recipients).orElse(Lists.newArrayList());
    }
}
