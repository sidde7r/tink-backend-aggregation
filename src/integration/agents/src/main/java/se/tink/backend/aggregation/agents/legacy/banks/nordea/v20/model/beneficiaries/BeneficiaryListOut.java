package se.tink.backend.aggregation.agents.banks.nordea.v20.model.beneficiaries;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BeneficiaryListOut {

    private static final TypeReference<List<BeneficiaryEntity>> LIST_TYPE_REFERENCE =
            new TypeReference<List<BeneficiaryEntity>>() {};
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("beneficiary")
    private List<BeneficiaryEntity> beneficiaries;

    public List<BeneficiaryEntity> getBeneficiaries() {

        if (beneficiaries == null) {
            return Lists.newArrayList();
        }

        return beneficiaries;
    }

    /**
     * Nordea API is a bit weird and send items on different formats depending on the number of
     * items. Multiple rows means that we will get an List of items and one row will not be typed as
     * an array.
     */
    public void setBeneficiaries(Object input) {

        if (input instanceof Map) {
            beneficiaries = Lists.newArrayList(MAPPER.convertValue(input, BeneficiaryEntity.class));
        } else {
            beneficiaries = MAPPER.convertValue(input, LIST_TYPE_REFERENCE);
        }
    }
}
