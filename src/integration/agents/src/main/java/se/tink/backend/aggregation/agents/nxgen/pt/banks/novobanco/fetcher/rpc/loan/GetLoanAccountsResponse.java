package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntityWrapper;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan.GetLoanAccountsBody;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan.LoanOverviewEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan.LoanSectionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetLoanAccountsResponse extends HeaderEntityWrapper {
    @JsonProperty("Body")
    private GetLoanAccountsBody body;

    public GetLoanAccountsBody getBody() {
        return body;
    }

    public Collection<LoanOverviewEntity> getLoanDetails() {
        return Optional.of(getBody())
                .map(GetLoanAccountsBody::getSection)
                .map(LoanSectionEntity::getLoansOverview)
                .orElse(Collections.emptyList());
    }
}
