package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTransactionsRequest {
    @JsonProperty private int currentPageNumber;

    @JsonProperty("dataFim")
    private String dateTo;

    @JsonProperty("dataInicio")
    private String dateFrom;

    @JsonProperty("indiceConta")
    private String accountHandle;

    @JsonProperty private int nrRegistosPagina = 20;
    @JsonProperty private String pesUltMov = "false";

    public FetchTransactionsRequest(
            int currentPageNumber, LocalDate dateTo, LocalDate dateFrom, String accountHandle) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00+00:00");
        this.currentPageNumber = currentPageNumber;
        this.dateTo = dateFormat.format(dateTo);
        this.dateFrom = dateFormat.format(dateFrom);
        this.accountHandle = accountHandle;
    }
}
