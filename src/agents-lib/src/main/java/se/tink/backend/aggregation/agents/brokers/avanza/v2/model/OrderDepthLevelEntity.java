package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDepthLevelEntity {
    private AmountEntity sell;
    private AmountEntity buy;

    public AmountEntity getSell() {
        return sell;
    }

    public AmountEntity getBuy() {
        return buy;
    }
}
