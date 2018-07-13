package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

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
