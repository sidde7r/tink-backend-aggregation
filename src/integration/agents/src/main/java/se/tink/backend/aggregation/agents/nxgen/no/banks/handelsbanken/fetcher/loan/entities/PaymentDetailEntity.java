package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.entities;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class PaymentDetailEntity {
    private long year;
    private double yearSum;
    private List<DetailsEntity> details;
}
