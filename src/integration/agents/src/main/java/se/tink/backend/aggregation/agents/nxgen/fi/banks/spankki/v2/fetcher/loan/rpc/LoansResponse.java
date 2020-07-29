package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class LoansResponse extends SpankkiResponse {
    @JsonProperty private Boolean hasLoans;
    @JsonProperty private BigDecimal totalBalance;

    @JsonIgnore
    private static final AggregationLogger logger = new AggregationLogger(LoansResponse.class);

    @JsonIgnore
    public boolean hasLoans() {
        return hasLoans;
    }

    @JsonIgnore
    public void logLoans() {
        logger.infoExtraLong(
                SerializationUtils.serializeToString(this), SpankkiConstants.LogTags.LOAN);
    }
}
