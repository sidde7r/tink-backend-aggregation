package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class SettlementEntity {

    private List<FeesItemEntity> fees;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date chargeValueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date paymentValueDate;

    private AmountEntity amount;
}
