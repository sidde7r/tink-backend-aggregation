package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities.PortfolioContentEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

// Not sure how this data structure really looks or works
// seems like there is one list with portfolios and
// one list with portfolio contents
// there is a strange "masDatos" in the structure
// not sure how to fetch those data
@JsonObject
public class PositionValuesResponse {
    @JsonProperty("idExpediente")
    private String id;

    private String alias;

    @JsonProperty("valoracionExpedientes")
    private BalanceEntity currentValue;

    @JsonProperty("plusvaliaExpedientes")
    private BalanceEntity valueChange;

    private boolean mostrarPlusvaliaRentabilidad;

    @JsonProperty("numeroExpediente28")
    private String accountNumber;

    @JsonProperty("listaExpedientes")
    private List<PortfolioEntity> portfolioList;

    @JsonProperty("listaBloques")
    private List<PortfolioContentEntity> portfolioContents;

    private String descripcionTiempoExcedido;
    private boolean tiempoExcedido;

    public List<PortfolioEntity> getPortfolioList() {
        return portfolioList;
    }

    public List<PortfolioContentEntity> getPortfolioContents() {
        return portfolioContents;
    }
}
