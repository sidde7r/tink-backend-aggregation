package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.entities;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class LoanAccountEntity {
    private String id;
    private String type;
    private String accountDescription;
    private BigDecimal balance;
    private Map<String, LinkEntity> links;
}
