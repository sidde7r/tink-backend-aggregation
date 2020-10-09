package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    private final List<String> accounts = Collections.emptyList();
    private final List<String> transactions = Collections.emptyList();
    private final List<String> balances = Collections.emptyList();
}
