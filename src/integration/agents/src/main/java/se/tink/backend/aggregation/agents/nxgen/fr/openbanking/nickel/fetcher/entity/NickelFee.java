package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity;

import java.math.BigDecimal;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class NickelFee {
    BigDecimal amount;
}
