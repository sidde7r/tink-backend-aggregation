package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class LoanDetailsResponse extends SpankkiResponse {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @JsonProperty private BigDecimal totalDebtValue;
    @JsonProperty private List<LoansEntity> loans;

    @JsonIgnore
    public void logLoans() {
        logger.info(
                "tag={} {}",
                SpankkiConstants.LogTags.LOAN_DETAILS,
                SerializationUtils.serializeToString(this));
    }
}
