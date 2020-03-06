package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityOutputEntity {
    @JsonProperty("TPC2CardDataList")
    private List<KeyValueEntity> cardData;

    @JsonProperty("TPC2CardImage")
    private String cardImage;

    @JsonProperty("TPC2CardSeq")
    private String cardSeq;

    private FloatingKeyboardEntity floatingKeyboard;
    private String tpcenabled;

    public FloatingKeyboardEntity getFloatingKeyboard() {
        return floatingKeyboard;
    }

    public List<KeyValueEntity> getCardData() {
        return cardData;
    }
}
