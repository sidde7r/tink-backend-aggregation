package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Characteristics {

    @JsonProperty("bankId")
    private String bankId;

    @JsonProperty("subscribeTypeItems")
    private List<SubscribeTypeItemsItem> subscribeTypeItems;

    @JsonProperty("iTEntityType")
    private ITEntityType iTEntityType;

    @JsonProperty("userCode")
    private String userCode;

    // Getters are not generated with lombok because that caused the serialization to generate 2
    // iTEntityType entries.
    public String getBankId() {
        return bankId;
    }

    public List<SubscribeTypeItemsItem> getSubscribeTypeItems() {
        return subscribeTypeItems;
    }

    public ITEntityType getiTEntityType() {
        return iTEntityType;
    }

    public String getUserCode() {
        return userCode;
    }
}
