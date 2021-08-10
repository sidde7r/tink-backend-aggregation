package se.tink.backend.aggregation.agents.banks.sbab.entities;

import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BlancosEntity {
    private BigDecimal loanAmount;
    private BigInteger loanNumber;
    private String loanType;
}
