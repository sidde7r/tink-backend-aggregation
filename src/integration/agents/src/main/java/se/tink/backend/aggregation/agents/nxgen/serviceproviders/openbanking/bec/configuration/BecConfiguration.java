package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BecConfiguration implements ClientConfiguration {

    private String enrollmentID;
    private String eidasQwac;

    public String getEnrollmentId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(enrollmentID),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Enrollment ID"));
        return enrollmentID;
    }

    public String getEidasQwac() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(eidasQwac),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Eidas Qwac"));
        return eidasQwac;
    }
}
