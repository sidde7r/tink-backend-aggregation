package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LivPensionsResponseModelEntity {
    private List<PrivatePensionsEntity> privatePensions;
    private List<OccupationalPensionsEntity> occupationalPensions;
    private List<CapitalInsurancesEntity> capitalInsurances;
  // `error` is null - cannot define it!
}