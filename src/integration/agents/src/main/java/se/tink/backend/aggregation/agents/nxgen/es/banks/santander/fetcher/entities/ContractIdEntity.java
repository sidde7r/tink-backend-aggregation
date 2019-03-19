package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class ContractIdEntity {
    @JsonProperty("OFICINA")
    private Office office;

    @JsonProperty("DIGITO_DE_CONTROL")
    private String controlDigits;

    @JsonProperty("NUMERO_DE_CUENTA")
    private String accountNumber;

    public Office getOffice() {
        return office;
    }

    public String getControlDigits() {
        return controlDigits;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
