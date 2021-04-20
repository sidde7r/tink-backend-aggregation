package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingInvestmentEntity {
    private String associatedAccount;
    private String policyAccount;
    private String depositId;
    private String description;
    private BigDecimal amount;
    private String product;
}
