package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class VolumePriceEntity {
    private BigDecimal volume;
    private BigDecimal price;
}
