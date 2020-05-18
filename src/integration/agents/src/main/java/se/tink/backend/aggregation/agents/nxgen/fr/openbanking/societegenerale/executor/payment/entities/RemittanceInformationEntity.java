package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;


import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class RemittanceInformationEntity {
    public List<String> getUnstructured() {
        return unstructured;
    }

    public void setUnstructured(List<String> unstructured) {
        this.unstructured = unstructured;
    }

    private List<String> unstructured;

}
