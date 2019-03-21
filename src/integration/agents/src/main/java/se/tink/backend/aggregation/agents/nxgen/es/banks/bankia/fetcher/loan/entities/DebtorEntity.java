package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DebtorEntity {
    private String codigoTipoDocumentoIdentificativo;
    private String identificadorDocumentoCliente;
    @JsonProperty("nombreRazonSocial")
    private String debtorName;
    private String identificadorCliente;
    @JsonProperty("codigoTipoIntervencion")
    private String debtorRoleCode;
    @JsonProperty("nombreTipoIntervencion")
    private String debtorRoleName;
    private int numeroOrdenTipoIntervencion;
    private PercentageEntity porcentajeExtincionFiador;
    private PercentageEntity porcentajeParticipacion;
    private boolean indicadorClienteSeguimiento;

    public String getDebtorName() {
        return debtorName;
    }

    public String getDebtorRoleCode() {
        return debtorRoleCode;
    }
}
