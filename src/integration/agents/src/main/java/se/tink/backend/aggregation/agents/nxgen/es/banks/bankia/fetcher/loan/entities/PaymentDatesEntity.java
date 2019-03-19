package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentDatesEntity {
    private int diaVencimientoCuotas;
    private DateEntity fechaAnteriorVencimiento;
    private DateEntity fechaProximoVencimiento;
//    private List<CodigoMesAlternoEntity> codigoMesAlterno;  // no values for this yet
}
