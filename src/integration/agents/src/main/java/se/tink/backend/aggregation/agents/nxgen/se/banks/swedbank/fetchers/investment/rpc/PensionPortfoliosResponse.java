package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.entities.OccupationalPensionInsurancesEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.entities.PensionInsuranceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.entities.PrivatePensionInsurancesEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractAccountEntity;
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

    @JsonIgnore
    private List<PensionInsuranceEntity> getAllPensionInsurances() {
        final List<PensionInsuranceEntity> allPensionInsurances = new ArrayList<>();

        if (privatePensionInsurances != null) {
            allPensionInsurances.addAll(privatePensionInsurances.getPensionInsurances());
        }

        if (occupationalPensionInsurances != null) {
            allPensionInsurances.addAll(occupationalPensionInsurances.getPensionInsurances());
        }

        return allPensionInsurances;
    }

    /**
     * Temporary method to know if a user has holdings so that we can look at the responses in the
     * S3 logs.
     *
     * @return True if holdings list is non-empty for any of the pension accounts.
     */
    @JsonIgnore
    public boolean hasPensionHoldings() {
        return getAllPensionInsurances().stream()
                .anyMatch(PensionInsuranceEntity::hasNonEmptyHoldingsList);
    }

    /**
     * Pension accounts appear both under pension portfolios and engagement overview (as savings
     * accounts). The the transactional account fetcher should exclude them so they are not
     * duplicated.
     */
    @JsonIgnore
    public List<String> getPensionAccountNumbers() {
        return getAllPensionInsurances().stream()
                .map(AbstractAccountEntity::getFullyFormattedNumber)
                .collect(Collectors.toList());
    }
}
