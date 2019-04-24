package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    private AmountEntity amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    public AmountEntity getAmount() {
        return amount;
    }
}
