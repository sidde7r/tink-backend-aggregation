package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.FamilyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractEntity {
    @JsonProperty("codigoProductoUrsus")
    private String productCode;
    @JsonProperty("codigoProductoPersonalizado")
    private String customizedProductCode;
    @JsonProperty("identificadorContratoProducto")
    private String identifierProductContract;
    @JsonProperty("identificadorContratoProductoInterno")
    private String identifierProductContractInternal;
    @JsonProperty("entidadDelProducto")
    private String productEntity;
    private String alias;
    @JsonProperty("nivelOperatividad")
    private String operabilityLevel;
    @JsonProperty("tipoRelacionContratoUsuario")
    private String typeContractUserAgreement;
    @JsonProperty("orden")
    private int order;
    @JsonProperty("familia")
    private FamilyEntity family;
    private boolean idVista1;
    private boolean idVista2;
    private boolean idVista3;
    private boolean idVista4;
    @JsonProperty("indicadorProductoNuevo")
    private boolean productNewIndicator;
    @JsonProperty("numeroFirmasProducto")
    private int numberOfFirmsProduct;
    @JsonProperty("indicadorTitularResidenteEnEspana")
    private boolean indicatorResidentHolderInSpain;
    @JsonProperty("identificadorTipoNaturaleza")
    private String typeNatureIdentifier;
    @JsonProperty("oficinaContrato")
    private String contractOffice;
    @JsonProperty("decimalesParticipacionesFondo")
    private int decimalsHoldingsFund;
    private String isin;
    @JsonProperty("tipoTarjeta")
    private String cardType;
    @JsonProperty("tarjetaConChip")
    private boolean cardWithChip;
    @JsonProperty("tarjetaEsMulti")
    private boolean esMultiCard;
    @JsonProperty("urlPlasticoTarjeta")
    private String plasticUrlCard;

    public String getProductCode() {
        return productCode;
    }

    public String getCustomizedProductCode() {
        return customizedProductCode;
    }

    public String getIdentifierProductContract() {
        return identifierProductContract;
    }

    public String getIdentifierProductContractInternal() {
        return identifierProductContractInternal;
    }

    public String getProductEntity() {
        return productEntity;
    }

    public String getAlias() {
        return alias;
    }

    public String getOperabilityLevel() {
        return operabilityLevel;
    }

    public String getTypeContractUserAgreement() {
        return typeContractUserAgreement;
    }

    public int getOrder() {
        return order;
    }

    public FamilyEntity getFamily() {
        return family;
    }

    public boolean isIdVista1() {
        return idVista1;
    }

    public boolean isIdVista2() {
        return idVista2;
    }

    public boolean isIdVista3() {
        return idVista3;
    }

    public boolean isIdVista4() {
        return idVista4;
    }

    public boolean isProductNewIndicator() {
        return productNewIndicator;
    }

    public int getNumberOfFirmsProduct() {
        return numberOfFirmsProduct;
    }

    public boolean isIndicatorResidentHolderInSpain() {
        return indicatorResidentHolderInSpain;
    }

    public String getTypeNatureIdentifier() {
        return typeNatureIdentifier;
    }

    public String getContractOffice() {
        return contractOffice;
    }

    public int getDecimalsHoldingsFund() {
        return decimalsHoldingsFund;
    }

    public String getIsin() {
        return isin;
    }

    public String getCardType() {
        return cardType;
    }

    public boolean isCardWithChip() {
        return cardWithChip;
    }

    public boolean isEsMultiCard() {
        return esMultiCard;
    }

    public String getPlasticUrlCard() {
        return plasticUrlCard;
    }
}
