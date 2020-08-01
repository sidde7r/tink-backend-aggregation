package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class LoansResponse extends SpankkiResponse {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @JsonProperty private Boolean hasLoans;
    @JsonProperty private BigDecimal totalBalance;

    @JsonIgnore
    public boolean hasLoans() {
        return hasLoans;
    }

    @JsonIgnore
    public void logLoans() {
        logger.info(
                "tag={} {}",
                SpankkiConstants.LogTags.LOAN,
                SerializationUtils.serializeToString(this));
    }
}
