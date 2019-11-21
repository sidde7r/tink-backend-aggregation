package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.investment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseCodes;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.StatusEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.investment.GetInvestmentsBodyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetInvestmentsResponse {
    private static final Logger logger = LoggerFactory.getLogger(GetInvestmentsResponse.class);

    @JsonProperty("Header")
    private HeaderEntity header;

    @JsonProperty("Body")
    private GetInvestmentsBodyEntity body;

    public HeaderEntity getHeader() {
        return header;
    }

    public GetInvestmentsBodyEntity getBody() {
        return body;
    }

    public boolean isSuccessful() {
        Integer resultCode = getResultCode();
        boolean isSuccessful = Objects.equals(ResponseCodes.OK, getResultCode());

        if (!isSuccessful) {
            logger.warn("ObterCarteiraFundos Response ended up with failure code: " + resultCode);
        }
        return isSuccessful;
    }

    private Integer getResultCode() {
        return Optional.ofNullable(getHeader())
                .map(HeaderEntity::getStatus)
                .map(StatusEntity::getCode)
                .orElse(null);
    }
}
