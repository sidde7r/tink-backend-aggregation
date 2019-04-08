package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.getsigningprotocol.RPScenarioEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer.BeneficiariesContacts;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DynamiccontentEntity {
    @JsonProperty("RP_Scenario")
    private RPScenarioEntity rPScenario;

    private String key;

    @JsonProperty("repeatedPane_BeneficiariesContacts")
    private BeneficiariesContacts repeatedPaneBeneficiariesContacts;

    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public BeneficiariesContacts getRepeatedPaneBeneficiariesContacts() {
        return repeatedPaneBeneficiariesContacts;
    }

    public void setRepeatedPaneBeneficiariesContacts(
            BeneficiariesContacts repeatedPaneBeneficiariesContacts) {
        this.repeatedPaneBeneficiariesContacts = repeatedPaneBeneficiariesContacts;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getSignType() {
        return (rPScenario == null) ? "" : rPScenario.getSignType();
    }
}
