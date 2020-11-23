package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.fetcher.rpc.entities;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class AmountEntity {

    private String currency;
    private BigDecimal value;
}
