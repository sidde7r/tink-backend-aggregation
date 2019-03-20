package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class GeneralInfoEntity {
    @JsonProperty("contratoID")
    private ContractEntity contractId;

    @JsonProperty("descContrato")
    private String contractDescription;

    private String alias;

    @JsonProperty("subtipoProd")
    private SubProductTypeEntity subProductType;

    @JsonProperty("tipoInterv")
    private String typeInterv;

    @JsonProperty("descTipoInterv")
    private String typeIntervalDescription;

    @JsonProperty("indVisibleAlias")
    private String indicatorVisibleAlias;

    public ContractEntity getContractId() {
        return contractId;
    }

    public String getContractDescription() {
        return contractDescription;
    }

    public String getAlias() {
        return alias;
    }

    public SubProductTypeEntity getSubProductType() {
        return subProductType;
    }

    public String getTypeInterv() {
        return typeInterv;
    }

    public String getTypeIntervalDescription() {
        return typeIntervalDescription;
    }

    public String getIndicatorVisibleAlias() {
        return indicatorVisibleAlias;
    }
}
