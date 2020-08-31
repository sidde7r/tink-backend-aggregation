package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsRequestPayload {
    @JsonProperty("dataFine")
    private String dateToInEpochMilis;

    @JsonProperty("dataInizio")
    private String dateFromInEpochMilis = "0"; // 1970-01-01

    private boolean flagSalvadanaio = false;
    private String flagStatoOperazione = "TUTTI";
    private List<Object> identificativiFiscali = new ArrayList<>(0);

    @JsonProperty("numeroOperazioniOutput")
    private int pageSize = 15;

    @JsonProperty("paginaRichiesta")
    private int pageNumber;

    @JsonProperty("rapporti")
    private List<AccountIdEntity> accountIdentifiers;

    @JsonProperty("recordPerPagina")
    private int transactionsPerPage = 15;

    public TransactionsRequestPayload(String accountId, LocalDate dateTo, int pageNumber) {
        this.dateToInEpochMilis =
                "" + dateTo.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
        this.accountIdentifiers = Collections.singletonList(new AccountIdEntity(accountId));
        this.pageNumber = pageNumber;
    }
}
