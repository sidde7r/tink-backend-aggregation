package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.Payload;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.ResultInfoMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.SystemStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.UserInformation;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.PendingTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.TransactionQuery;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Response {
    @JsonProperty("d")
    public Payload payload = new Payload();

    @JsonProperty("X")
    public SystemStatus systemStatus = new SystemStatus();

    /**
     * Request is an error of the messages size are larger than 0. There can be several error
     * messages on a response.
     */
    @JsonIgnore
    public boolean hasErrors() {
        return payload != null && payload.hasErrors();
    }

    @JsonIgnore
    public List<ResultInfoMessage> getErrors() {
        if (hasErrors()) {
            return payload.getMessages();
        } else {
            return ImmutableList.of();
        }
    }

    // Some errors might be missing an error text
    @JsonIgnore
    public Optional<ResultInfoMessage> getFirstErrorWithErrorText() {
        return getErrors().stream().filter(ResultInfoMessage::hasText).findFirst();
    }

    @JsonIgnore
    public Optional<String> getFirstErrorMessage() {
        return getFirstErrorWithErrorText().map(ResultInfoMessage::getErrorText);
    }

    /** Checks that the response has a VODB object */
    @JsonIgnore
    public boolean isValid() {
        if (hasErrors()) {
            return false;
        }

        if (Objects.isNull(payload) || !payload.isValid()) {
            return false;
        }
        return true;
    }

    @JsonIgnore
    public String getInitResult() {
        Preconditions.checkState(isValid());
        return payload.getInitResult().getInitResult();
    }

    @JsonIgnore
    public SystemStatus getSystemStatus() {
        return systemStatus;
    }

    @JsonIgnore
    public UserInformation getUserInformation() {
        return payload.getUserInformation();
    }

    @JsonIgnore
    public List<AccountEntity> getAccountEntities() {
        return payload.getAccountEntities();
    }
}
