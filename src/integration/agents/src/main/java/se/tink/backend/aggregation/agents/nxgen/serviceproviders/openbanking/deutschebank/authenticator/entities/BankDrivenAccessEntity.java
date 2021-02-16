package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankDrivenAccessEntity implements AccessEntity {
    private List<String> accounts = Collections.emptyList();
    private List<String> transactions = Collections.emptyList();
    private List<String> balances = Collections.emptyList();
    private AdditionalInformation additionalInformation = new AdditionalInformation();
}
