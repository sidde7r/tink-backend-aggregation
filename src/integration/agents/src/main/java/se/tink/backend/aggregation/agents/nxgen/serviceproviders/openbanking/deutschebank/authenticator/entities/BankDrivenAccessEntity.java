package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankDrivenAccessEntity implements AccessEntity {
    List<String> accounts = Collections.emptyList();
    List<String> transactions = Collections.emptyList();
    List<String> balances = Collections.emptyList();
    AdditionalInformation additionalInformation = new AdditionalInformation();
}
