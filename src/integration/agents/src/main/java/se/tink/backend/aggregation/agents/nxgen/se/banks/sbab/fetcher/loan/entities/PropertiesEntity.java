package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PropertiesEntity {
    private List<PropertyLoansEntity> propertyLoans;
    private String propertyName;
    private BigDecimal totalDebt;
}
