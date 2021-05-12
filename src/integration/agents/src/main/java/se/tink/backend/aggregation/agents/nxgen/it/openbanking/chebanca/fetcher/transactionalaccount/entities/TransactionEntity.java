package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionEntity {
    private AmountEntity amountTransaction;
    private String extendedDescription;
    private String dateAccountingCurrency;
    private String shortDescription;
    private String dateLiquidationValue;
    private String codeDescription;
}
