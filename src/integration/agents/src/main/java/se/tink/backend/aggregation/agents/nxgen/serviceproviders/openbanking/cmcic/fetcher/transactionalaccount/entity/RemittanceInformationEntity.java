package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RemittanceInformationEntity {
    @JsonProperty("unstructured")
    private List<String> unstructured = null;

    @JsonProperty("structured")
    private List<StructuredRemittanceInformationEntity> structured = null;

    public List<String> getUnstructured() {
        return unstructured;
    }

    public void setUnstructured(List<String> unstructured) {
        this.unstructured = unstructured;
    }

    public List<StructuredRemittanceInformationEntity> getStructured() {
        return structured;
    }

    public void setStructured(List<StructuredRemittanceInformationEntity> structured) {
        this.structured = structured;
    }
}
