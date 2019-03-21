package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CommissionsEntity {
    private AmountEntity importeMinimoComisionApertura;
    private PercentageEntity porcentajeComisionAmortizacionMenor25;
    private PercentageEntity porcentajeComisionAmortizacionMayor25;
    private PercentageEntity porcentajeComisionApertura;
}
