package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestTypesEntity {
    @JsonProperty("porcentajeCERVigente")
    private PercentageEntity percentageRemainingCashCost; // remaining cash cost

    private String codigoModalidadTipoInteres;

    @JsonProperty("porcentajeTAE")
    private PercentageEntity percentageAnnualEquivalence; // Annual Equivalence Rate

    @JsonProperty("porcentajeTipoInteresContrato")
    private PercentageEntity percentageContract; // contract

    @JsonProperty("porcentajeTipoInteresCobro")
    private PercentageEntity percentageCollection; // collection

    private PercentageEntity porcentajeTipoInteresDiferimiento;

    public PercentageEntity getPercentageContract() {
        return percentageContract;
    }
}
