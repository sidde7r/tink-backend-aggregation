package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RemittanceInfoEntity {
    private String creditorFriendlyName;
    private String debtorAccountStatementText;
}
