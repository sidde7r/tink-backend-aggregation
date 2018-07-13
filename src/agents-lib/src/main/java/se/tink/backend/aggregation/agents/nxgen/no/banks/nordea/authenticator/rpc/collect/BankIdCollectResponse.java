package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.collect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.entities.collect.PollBankIDSIMAuthenticationOutEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.rpc.NordeaResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdCollectResponse extends NordeaResponse {
    private static final AggregationLogger log = new AggregationLogger(BankIdCollectResponse.class);

    @JsonProperty("pollBankIDSIMAuthenticationResponse")
    private PollBankIDSIMAuthenticationOutEntity pollBankIDSIMAuthenticationOutEntity;

    public PollBankIDSIMAuthenticationOutEntity getPollBankIDSIMAuthenticationOutEntity() {
        return pollBankIDSIMAuthenticationOutEntity;
    }

    @JsonIgnore
    public BankIdStatus getStatus() {
        Optional<String> errorCode = super.getErrorCode();

        if (errorCode.isPresent()) {
            switch (errorCode.get()) {
            case NordeaNoConstants.BankIdStatus.CANCELLED:
                return BankIdStatus.CANCELLED;
            case NordeaNoConstants.BankIdStatus.TIMEOUT:
                return BankIdStatus.TIMEOUT;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
            }
        }

        String rawStatus = pollBankIDSIMAuthenticationOutEntity.getProgressStatus();
        switch (rawStatus) {
        case NordeaNoConstants.BankIdStatus.WAITING:
            return BankIdStatus.WAITING;
        case NordeaNoConstants.BankIdStatus.COMPLETE:
            return BankIdStatus.DONE;
        default:
            log.error(String.format("Nordea (NO) - Unknown BankID status (%s)", rawStatus));
            return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    @JsonIgnore
    public String getToken() {
        return pollBankIDSIMAuthenticationOutEntity.getAuthenticationToken().getToken();
    }

    @Override
    public Optional<String> getErrorCode() {
        return Objects.equals(getStatus(), BankIdStatus.FAILED_UNKNOWN) ? super.getErrorCode() : Optional.empty();
    }
}
