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
    private int controlDigits;
    @JsonProperty("NUMERO_DE_CUENTA")
    private long accountNumber;

    public Office getOffice() {
        return office;
    }

    public int getControlDigits() {
        return controlDigits;
    }

    public long getAccountNumber() {
        return accountNumber;
    }
}
