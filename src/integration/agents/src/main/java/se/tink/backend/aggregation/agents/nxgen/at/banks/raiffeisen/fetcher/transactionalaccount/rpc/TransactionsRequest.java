package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.entities.IbansListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsRequest {
    private Long offset;
    private Long limit;
    @JsonProperty("predicate")
    private IbansListEntity ibans;
    @JsonProperty("buchungVon")
    private String fromDate;
    @JsonProperty("buchungBis")
    private String toDate;
    private Boolean prependInfo;

    public TransactionsRequest(String iban, Date fromDate, Date toDate) {
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        this.offset = 0L;
        this.limit = 10000L;
        this.ibans = new IbansListEntity(iban);
        this.fromDate = df.format(fromDate);
        this.toDate = df.format(toDate);
        this.prependInfo = Boolean.FALSE;
    }
}
