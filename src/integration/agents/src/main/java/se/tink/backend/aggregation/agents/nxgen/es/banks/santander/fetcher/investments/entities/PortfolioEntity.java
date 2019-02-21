package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.ContractIdEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.GeneralInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class PortfolioEntity {

    @JsonProperty("comunes")
    private GeneralInfoEntity generalInfo;
    @JsonProperty("contratoID")
    private ContractIdEntity contractId;
    @JsonProperty("impValoracion")
    private AmountEntity totalValue;
    @JsonProperty("impValoracionContravalor")
    private AmountEntity counterTotalValue;
    @JsonProperty("codReferencia")
    private String referenceCode;

    public GeneralInfoEntity getGeneralInfo() {
        return generalInfo;
    }

    public ContractIdEntity getContractId() {
        return contractId;
    }

    public AmountEntity getTotalValue() {
        return totalValue;
    }

    public AmountEntity getCounterTotalValue() {
        return counterTotalValue;
    }

    public String getReferenceCode() {
        return referenceCode;
    }
}
