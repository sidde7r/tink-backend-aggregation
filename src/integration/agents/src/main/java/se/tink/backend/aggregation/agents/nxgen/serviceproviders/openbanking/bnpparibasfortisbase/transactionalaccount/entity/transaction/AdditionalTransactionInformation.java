package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.transaction;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AdditionalTransactionInformation {

    private List<String> narrative;
    private String atmPosName;
    private String cardNumber;

    public String getDescription() {
        return narrative.stream()
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Missing value for additional information"));
    }
}
