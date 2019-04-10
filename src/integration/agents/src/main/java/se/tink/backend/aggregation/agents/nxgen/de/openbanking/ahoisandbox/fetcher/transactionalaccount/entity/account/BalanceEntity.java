package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    private AmountEntity amount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private String date;

    public AmountEntity getAmount() {
        return amount;
    }
}
