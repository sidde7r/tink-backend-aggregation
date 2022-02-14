package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonObject
public class ConsentStatus {

    private static final String RECEIVED = "received";
    private static final String REJECTED = "rejected";
    private static final String PARTIALLY_AUTHORISED = "partiallyAuthorised";
    private static final String VALID = "valid";
    private static final String REVOKED_BY_PSU = "revokedByPsu";
    private static final String EXPIRED = "expired";
    private static final String TERMINATED_BY_TPP = "terminatedByTpp";

    protected String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public boolean isReceived() {
        return is(RECEIVED);
    }

    public boolean isRejected() {
        return is(REJECTED);
    }

    public boolean isPartiallyAuthorised() {
        return is(PARTIALLY_AUTHORISED);
    }

    public boolean isValid() {
        return is(VALID);
    }

    public boolean isRevokedByPsu() {
        return is(REVOKED_BY_PSU);
    }

    public boolean isExpired() {
        return is(EXPIRED);
    }

    public boolean isTerminatedByTpp() {
        return is(TERMINATED_BY_TPP);
    }

    protected boolean is(String other) {
        return value != null && value.equalsIgnoreCase(other);
    }

    public boolean isFinal() {
        return isRejected() || isValid() || isRevokedByPsu() || isExpired() || isTerminatedByTpp();
    }

    @Override
    public String toString() {
        return value;
    }
}
