package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.BankOffice;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class ContractEntity {
    @JsonProperty("CENTRO")
    private BankOffice bankOffice;

    @JsonProperty("PRODUCTO")
    private String product;

    @JsonProperty("NUMERO_DE_CONTRATO")
    private String contractNumber;

    public BankOffice getBankOffice() {
        return bankOffice;
    }

    public String getProduct() {
        return product;
    }

    public String getContractNumber() {
        return contractNumber;
    }
}
