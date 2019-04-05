package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class TransactionBalanceEntity {

    private AmountEntity amount;
    private String creditDebitIndicator;
    private String type;
}
