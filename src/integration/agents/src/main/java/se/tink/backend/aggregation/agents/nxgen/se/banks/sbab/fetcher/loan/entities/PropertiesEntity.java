package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PropertiesEntity {
    private List<PropertyLoansEntity> propertyLoans;
    private String propertyName;
    private BigDecimal totalDebt;

    public List<PropertyLoansEntity> getPropertyLoans() {
        return propertyLoans;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public BigDecimal getTotalDebt() {
        return totalDebt;
    }
}
