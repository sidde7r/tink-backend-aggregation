package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RemittanceInformationEntity {

    private List<String> unstructured;

    public List<String> getUnstructured() {
        return unstructured;
    }
}
