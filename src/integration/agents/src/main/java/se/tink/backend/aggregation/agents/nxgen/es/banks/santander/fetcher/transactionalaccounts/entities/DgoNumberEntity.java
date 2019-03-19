package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.BankOffice;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class DgoNumberEntity {
    @JsonProperty("CENTRO")
    private BankOffice bankOffice;

    @JsonProperty("CODIGO_TERMINAL_DGO")
    private String dgoTerminalCode;

    @JsonProperty("NUMERO_DGO")
    private String dgoNumber;

    public BankOffice getBankOffice() {
        return bankOffice;
    }

    public String getDgoTerminalCode() {
        return dgoTerminalCode;
    }

    public String getDgoNumber() {
        return dgoNumber;
    }
}
