package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.transaction;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class RemittanceInformationEntity {
    private List<CreditorReferenceInformationEntity> structured;
    private List<String> unstructured;

    @Override
    public String toString() {
        return structured != null ? getStructuredInformation() : getUnstructuredInformation();
    }

    private String getStructuredInformation() {
        return structured.stream()
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Missing value for structured remittance information"))
                .toString();
    }

    public String getUnstructuredInformation() {
        return unstructured.stream()
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Missing value for unstructured remittance information"));
    }
}
