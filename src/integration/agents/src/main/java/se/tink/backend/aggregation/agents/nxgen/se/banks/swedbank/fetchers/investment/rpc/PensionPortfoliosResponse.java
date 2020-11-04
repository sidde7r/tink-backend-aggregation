package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.entities.OccupationalPensionInsurancesEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.entities.PensionInsuranceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.entities.PrivatePensionInsurancesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PensionPortfoliosResponse {
    private PrivatePensionInsurancesEntity privatePensionInsurances;
    private OccupationalPensionInsurancesEntity occupationalPensionInsurances;

    public PrivatePensionInsurancesEntity getPrivatePensionInsurances() {
        return privatePensionInsurances;
    }

    public OccupationalPensionInsurancesEntity getOccupationalPensionInsurances() {
        return occupationalPensionInsurances;
    }

    /**
     * Temporary method to know if a user has holdings so that we can look at the responses in the
     * S3 logs.
     *
     * @return True if holdings list is non-empty for any of the pension accounts.
     */
    @JsonIgnore
    public boolean hasPensionHoldings() {
        List<PensionInsuranceEntity> allPensionInsurances = new ArrayList<>();

        if (privatePensionInsurances != null) {
            allPensionInsurances.addAll(privatePensionInsurances.getPensionInsurances());
        }

        if (occupationalPensionInsurances != null) {
            allPensionInsurances.addAll(occupationalPensionInsurances.getPensionInsurances());
        }

        return allPensionInsurances.stream()
                .anyMatch(PensionInsuranceEntity::hasNonEmptyHoldingsList);
    }
}
