package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RecipientsResponseBody {
    @JsonProperty("Recipients")
    private List<RecipientEntity> recipients;

    public List<RecipientEntity> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<RecipientEntity> recipients) {
        this.recipients = recipients;
    }
}
