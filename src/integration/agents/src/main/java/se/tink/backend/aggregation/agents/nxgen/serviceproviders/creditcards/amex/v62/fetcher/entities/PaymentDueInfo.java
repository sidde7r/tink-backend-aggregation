package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentDueInfo {

    private String iconName;
    private List<String> messageAttributeValues;
    private String value;

    public String getIconName() {
        return iconName;
    }

    public List<String> getMessageAttributeValues() {
        return messageAttributeValues;
    }

    public String getValue() {
        return value;
    }
}
