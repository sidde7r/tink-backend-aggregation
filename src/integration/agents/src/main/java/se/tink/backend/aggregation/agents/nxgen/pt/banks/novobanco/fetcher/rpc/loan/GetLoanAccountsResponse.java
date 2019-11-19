package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan.GetLoanAccountsBody;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan.LoanOverviewEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan.LoanSectionEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetLoanAccountsResponse {
    private static final Logger logger = LoggerFactory.getLogger(GetAccountsResponse.class);

    @JsonProperty("Header")
    private HeaderEntity header;

    @JsonProperty("Body")
    private GetLoanAccountsBody body;

    public HeaderEntity getHeader() {
        return header;
    }

    public GetLoanAccountsBody getBody() {
        return body;
    }

    public Collection<LoanOverviewEntity> getLoanDetails() {
        return Optional.of(getBody())
                .map(GetLoanAccountsBody::getSection)
                .map(LoanSectionEntity::getLoansOverview)
                .orElse(Collections.emptyList());
    }

    public boolean isSuccessful() {
        Integer resultCode = getResultCode();
        boolean isSuccessful =
                Optional.ofNullable(resultCode)
                        .map(code -> NovoBancoConstants.ResponseCodes.OK == code)
                        .orElse(false);
        if (!isSuccessful) {
            logger.warn("GetLoanAccounts Response ended up with failure code: " + resultCode);
        }

        return isSuccessful;
    }

    private Integer getResultCode() {
        return Optional.ofNullable(getHeader()).map(HeaderEntity::getResultCode).orElse(null);
    }
}
