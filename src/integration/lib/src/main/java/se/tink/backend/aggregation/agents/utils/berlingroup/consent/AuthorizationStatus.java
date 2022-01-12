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
// This class is not an enum on purpose. The standard is quite precise on those values, but there
// can be exceptions.
public class AuthorizationStatus {
    private static final String RECEIVED = "received";
    private static final String PSU_IDENTIFIED = "psuIdentified";
    private static final String PSU_AUTHENTICATED = "psuAuthenticated";
    private static final String SCA_METHOD_SELECTED = "scaMethodSelected";
    private static final String STARTED = "started";
    private static final String UNCONFIRMED = "unconfirmed";
    private static final String FINALISED = "finalised";
    private static final String FAILED = "failed";
    private static final String EXEMPTED = "exempted";

    private String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public boolean isReceived() {
        return is(RECEIVED);
    }

    public boolean isPsuIdentified() {
        return is(PSU_IDENTIFIED);
    }

    public boolean isPsuAuthenticated() {
        return is(PSU_AUTHENTICATED);
    }

    public boolean isScaMethodSelected() {
        return is(SCA_METHOD_SELECTED);
    }

    public boolean isStarted() {
        return is(STARTED);
    }

    public boolean isUnconfirmed() {
        return is(UNCONFIRMED);
    }

    public boolean isFinalised() {
        return is(FINALISED);
    }

    public boolean isFailed() {
        return is(FAILED);
    }

    public boolean isExempted() {
        return is(EXEMPTED);
    }

    private boolean is(String other) {
        return value != null && value.equalsIgnoreCase(other);
    }

    @Override
    public String toString() {
        return value;
    }
}
