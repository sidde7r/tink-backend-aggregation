package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer.BeneficiariesContacts;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContentListEntity {
    @JsonProperty("staticcontent")
    private StaticcontentEntity staticcontent;
    @JsonProperty("dynamiccontent")
    private List<DynamiccontentEntity> dynamiccontent;
    private String type;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public StaticcontentEntity getStaticcontent() {
        return staticcontent;
    }

    public void setStaticcontent(StaticcontentEntity staticcontent) {
        this.staticcontent = staticcontent;
    }

    public List<DynamiccontentEntity> getDynamiccontent() {
        return dynamiccontent;
    }

    public void setDynamiccontent(List<DynamiccontentEntity> dynamiccontent) {
        this.dynamiccontent = dynamiccontent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public ArrayList<BeneficiariesContacts> getBeneficiaries() {
        ArrayList<BeneficiariesContacts> beneficiaries = new ArrayList<BeneficiariesContacts>();
        for (DynamiccontentEntity dc : dynamiccontent)
            if (dc != null && dc.getRepeatedPaneBeneficiariesContacts() != null) {
                beneficiaries.add(dc.getRepeatedPaneBeneficiariesContacts());
            }
        return beneficiaries;
    }

    public String getSignType() {
        if (dynamiccontent == null || dynamiccontent.size() == 0) {
            return "";
        }

        Optional<DynamiccontentEntity> optional = dynamiccontent.stream()
                .filter(dynamiccontentEntity -> !dynamiccontentEntity.getSignType().equals(""))
                .findFirst();
        return optional.isPresent() ? optional.get().getSignType() : "";
    }
}
