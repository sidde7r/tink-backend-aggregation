package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.rpc;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.entities.OperationCardLimitEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CreditCardResponse {
    private String creditAccountId;
    private String card;
    private String cardType;
    private String expiryDate;
    private Integer accumulatedOperations;
    private BigDecimal accumulatedAmount;
    private String discountForUse;
    private String domiciliationAccount;
    private String debitAccount;
    private String relatedMainCard;
    private String creditAccount;
    private String accountHolder;
    private Integer associatedCards;
    private String paymentMethod;
    private String liquidationPeriodicity;
    private String nextLiquidationDate;
    private BigDecimal availableAmount;
    private BigDecimal creditLimit;
    private BigDecimal cardLimit;
    private String currency;
    private List<OperationCardLimitEntity> operationCardLimits;
}
