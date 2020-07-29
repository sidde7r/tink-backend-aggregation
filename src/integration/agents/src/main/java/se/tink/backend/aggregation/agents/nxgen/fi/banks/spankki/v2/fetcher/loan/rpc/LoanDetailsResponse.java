package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class LoanDetailsResponse extends SpankkiResponse {
    @JsonProperty private BigDecimal totalDebtValue;
    @JsonProperty private List<LoansEntity> loans;

    @JsonIgnore
    private static final AggregationLogger logger =
            new AggregationLogger(LoanDetailsResponse.class);

    @JsonIgnore
    public void logLoans() {
        logger.infoExtraLong(
                SerializationUtils.serializeToString(this), SpankkiConstants.LogTags.LOAN_DETAILS);
    }
}
