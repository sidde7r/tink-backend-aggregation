package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TermsAndPeriodicitiesEntity {
    private QuantityEntity periodicidadDesembolso;
    private QuantityEntity periodicidadDiferimiento;
    private QuantityEntity plazoCarenciaPendiente;
    private QuantityEntity plazoDiferimientoPendiente;
    private QuantityEntity plazoDiferimiento;
    private QuantityEntity plazoPendienteUltimaFacturacion;
    private QuantityEntity plazoAmortizacion;
    private QuantityEntity plazoDesembolso;
    private QuantityEntity plazoCarencia;
    private QuantityEntity periodicidadAmortizacion;
    private QuantityEntity plazoTotal;
    private QuantityEntity periodicidadCarencia;
    private QuantityEntity periodicidadLiquidacionProductos;
    private QuantityEntity plazoPendienteTotal;
}
