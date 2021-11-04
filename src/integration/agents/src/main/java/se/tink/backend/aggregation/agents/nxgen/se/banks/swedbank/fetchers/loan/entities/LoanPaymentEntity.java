package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.DescriptiveAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoanPaymentEntity {
    private String description;
    private List<DescriptiveAmountEntity> amounts;
    private String totalAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDay;
}
