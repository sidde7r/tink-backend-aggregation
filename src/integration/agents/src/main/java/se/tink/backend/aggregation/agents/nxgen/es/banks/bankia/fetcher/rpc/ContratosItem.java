package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContratosItem {

    @JsonProperty("idVista2")
    private boolean idVista2;

    @JsonProperty("idVista3")
    private boolean idVista3;

    @JsonProperty("idVista4")
    private boolean idVista4;

    @JsonProperty("tipoRelacionContratoUsuario")
    private String tipoRelacionContratoUsuario;

    @JsonProperty("entidadDelProducto")
    private String entidadDelProducto;

    @JsonProperty("indicadorProductoNuevo")
    private boolean indicadorProductoNuevo;

    @JsonProperty("codigoProductoUrsus")
    private String codigoProductoUrsus;

    @JsonProperty("codigoProductoPersonalizado")
    private String codigoProductoPersonalizado;

    @JsonProperty("numeroFirmasProducto")
    private int numeroFirmasProducto;

    @JsonProperty("idVista1")
    private boolean idVista1;

    @JsonProperty("identificadorContratoProducto")
    private String identificadorContratoProducto;

    @JsonProperty("alias")
    private String alias;

    @JsonProperty("orden")
    private int orden;

    @JsonProperty("indicadorTitularResidenteEnEspana")
    private boolean indicadorTitularResidenteEnEspana;

    @JsonProperty("familia")
    private Familia familia;

    @JsonProperty("nivelOperatividad")
    private String nivelOperatividad;

    public boolean isIdVista2() {
        return idVista2;
    }

    public boolean isIdVista3() {
        return idVista3;
    }

    public boolean isIdVista4() {
        return idVista4;
    }

    public String getTipoRelacionContratoUsuario() {
        return tipoRelacionContratoUsuario;
    }

    public String getEntidadDelProducto() {
        return entidadDelProducto;
    }

    public boolean isIndicadorProductoNuevo() {
        return indicadorProductoNuevo;
    }

    public String getCodigoProductoUrsus() {
        return codigoProductoUrsus;
    }

    public String getCodigoProductoPersonalizado() {
        return codigoProductoPersonalizado;
    }

    public int getNumeroFirmasProducto() {
        return numeroFirmasProducto;
    }

    public boolean isIdVista1() {
        return idVista1;
    }

    public String getIdentificadorContratoProducto() {
        return identificadorContratoProducto;
    }

    public String getAlias() {
        return alias;
    }

    public int getOrden() {
        return orden;
    }

    public boolean isIndicadorTitularResidenteEnEspana() {
        return indicadorTitularResidenteEnEspana;
    }

    public Familia getFamilia() {
        return familia;
    }

    public String getNivelOperatividad() {
        return nivelOperatividad;
    }
}
