package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractEntity {
    @JsonProperty("expediente")
    private ContractIdEntity contractId;
    private String codigoContrato;
    private String descripcionContrato;
    @JsonProperty("contratoLOFormateado")
    private String contractNumberFormatted;
    @JsonProperty("codigoProducto")
    private String productCode;
    @JsonProperty("aliasContrato")
    private String contractAlias;
    private String codigoDivisa;
    private String literalDivisa;
    private String importeEuros;
    private String importeDivisaOriginal;
    private String limite;
    private String limiteDivisaOriginal;
    private String concedido;
    private String concedidoDivisaOriginal;
    private String fechaAlta;
    private String importeDisponibleComercial;
    private String nombreAplicacion;
    private String sena1210;
    private String empresaEmisora;
    private String codigoAgrupacionNivel10;
    private String literalAgrupacionNivel10;
    private String codigoAgrupacionNivel20;
    private String literalAgrupacionNivel20;

    @JsonIgnore
    public String getContractNumber() {
        return contractId.getContractNumber();
    }

    public String getProductCode() {
        return productCode;
    }

    public String getContractNumberFormatted() {
        return contractNumberFormatted;
    }

    public String getContractAlias() {
        return contractAlias;
    }
}
