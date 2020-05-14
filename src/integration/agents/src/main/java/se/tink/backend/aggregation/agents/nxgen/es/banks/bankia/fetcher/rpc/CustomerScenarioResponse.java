package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerScenarioResponse {

    @JsonProperty("tipoDocumento")
    private String tipoDocumento;

    @JsonProperty("usuarioEsPiloto2")
    private boolean usuarioEsPiloto2;

    @JsonProperty("tipoPersona")
    private String tipoPersona;

    @JsonProperty("identificadorCliente")
    private String identificadorCliente;

    @JsonProperty("usuarioEsPiloto1")
    private boolean usuarioEsPiloto1;

    @JsonProperty("cambioFirma")
    private boolean cambioFirma;

    @JsonProperty("cambioClave")
    private boolean cambioClave;

    @JsonProperty("contrato")
    private String contrato;

    @JsonProperty("contratos")
    private List<ContratosItem> contratos;

    @JsonProperty("documentoCliente")
    private String documentoCliente;

    @JsonProperty("usuarioEsPiloto4")
    private boolean usuarioEsPiloto4;

    @JsonProperty("usuarioEsPiloto3")
    private boolean usuarioEsPiloto3;

    public String getIdentificadorCliente() {
        return identificadorCliente;
    }

    public String getDocumentoCliente() {
        return documentoCliente;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public boolean isUsuarioEsPiloto2() {
        return usuarioEsPiloto2;
    }

    public String getTipoPersona() {
        return tipoPersona;
    }

    public boolean isUsuarioEsPiloto1() {
        return usuarioEsPiloto1;
    }

    public boolean isCambioFirma() {
        return cambioFirma;
    }

    public boolean isCambioClave() {
        return cambioClave;
    }

    public String getContrato() {
        return contrato;
    }

    public List<ContratosItem> getContratos() {
        return contratos;
    }

    public boolean isUsuarioEsPiloto4() {
        return usuarioEsPiloto4;
    }

    public boolean isUsuarioEsPiloto3() {
        return usuarioEsPiloto3;
    }
}
