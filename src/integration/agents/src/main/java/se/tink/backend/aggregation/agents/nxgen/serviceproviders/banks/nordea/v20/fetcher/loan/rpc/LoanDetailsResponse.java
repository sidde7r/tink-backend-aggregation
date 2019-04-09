package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan.entities.LoanData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan.entities.LoanPaymentDetails;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement()
public class LoanDetailsResponse extends NordeaResponse {
    @JsonProperty("getLoanDetailsOut")
    private LoanDetailsEntity loanDetails;

    public LoanDetailsEntity getLoanDetails() {
        return loanDetails;
    }

    @JsonIgnore
    public LoanData getLoanData() {
        return Optional.ofNullable(loanDetails).map(LoanDetailsEntity::getLoanData).orElse(null);
    }

    @JsonIgnore
    public LoanPaymentDetails getFollowingPayment() {
        return Optional.ofNullable(loanDetails)
                .map(LoanDetailsEntity::getFollowingPayment)
                .orElse(null);
    }
}
