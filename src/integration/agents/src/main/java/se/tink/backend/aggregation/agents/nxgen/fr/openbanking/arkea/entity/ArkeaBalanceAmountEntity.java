package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import java.math.BigDecimal;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaBalanceAmountEntity {

    private String currency;
    private BigDecimal amount;
}
