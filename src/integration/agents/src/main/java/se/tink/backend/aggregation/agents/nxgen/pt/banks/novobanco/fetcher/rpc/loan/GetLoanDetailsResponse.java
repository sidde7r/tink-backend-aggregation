package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan.GetLoanDetailsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetLoanDetailsResponse {
    private static final Logger logger = LoggerFactory.getLogger(GetAccountsResponse.class);

    @JsonProperty("Header")
    private HeaderEntity header;

    @JsonProperty("Body")
    private GetLoanDetailsBodyEntity body;

    public HeaderEntity getHeader() {
        return header;
    }

    public GetLoanDetailsBodyEntity getBody() {
        return body;
    }

    public boolean isSuccessful() {
        Integer resultCode = getResultCode();
        boolean isSuccessful =
                Optional.ofNullable(resultCode)
                        .map(code -> NovoBancoConstants.ResponseCodes.OK == code)
                        .orElse(false);
        if (!isSuccessful) {
            logger.warn("GetLoanDetails Response ended up with failure code: " + resultCode);
        }

        return isSuccessful;
    }

    private Integer getResultCode() {
        return Optional.ofNullable(getHeader()).map(HeaderEntity::getResultCode).orElse(null);
    }
}
