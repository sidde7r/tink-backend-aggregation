package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinancialMarketAccountEntity {

    private String accountId;
    private String account;
    private String profile;
    private String lastOperationDate;
    private String currency;
    private BigDecimal stockMarketBalance;
    private String stockMarketDate;
    private BigDecimal investmentFundBalance;
    private String investmentFundDate;
    private BigDecimal totalBalance;
}
