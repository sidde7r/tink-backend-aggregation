package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmortizationLiquidationEntity {
    private String codigoSistemaAmortizacion;
    private AmountEntity importeCuotaResidual;
    private QuantityEntity periodicidadProgesionCuota;
    private PercentageEntity porcentajeProgresionCuota;
}
