package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.entity.AgreementEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class MultipleAgreementsResponse {
    @JsonProperty("http_status")
    private int httpStatus;

    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    private List<AgreementEntity> agreements;
    private String country;

    @JsonIgnore
    public boolean isAgreementConflictError() {
        return NordeaBaseConstants.ErrorCodes.AGREEMENT_CONFLICT.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public Optional<String> getIdOfMatchingAgreement(String organisationNumber) {
        return ListUtils.emptyIfNull(agreements).stream()
                .filter(
                        agreementEntity ->
                                organisationNumber.equalsIgnoreCase(
                                        agreementEntity.getCustomerId()))
                .findAny()
                .map(AgreementEntity::getId);
    }
}
