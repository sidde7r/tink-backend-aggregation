package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.accountbalances;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountBalanceEntity {

    private String accountId;
    private AmountEntity amount;
    private String creditDebitIndicator;
    private List<CreditLineEntity> creditLine = null;
    private String dateTime;
    private String type;

    public boolean isInterimBooked() {
         return type.equalsIgnoreCase(
            AccountBalanceTypeEntity.BOOKED.getKey());
    }

    public boolean isInterimAvailable() {
        return type.equalsIgnoreCase(
            AccountBalanceTypeEntity.AVAILABLE.getKey());
    }

    public AmountEntity getAmount() {
        return amount;
    }
}
