package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.CallbackFields.CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.CallbackFields.DESCRIPTION;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.CallbackFields.STATE;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.CallbackFields;

public enum CmcicCallbackStatus {
    SUCCESS(ImmutableSet.of(CODE, STATE)), // WITH JAVA 9+ CAN BE REPLACED WITH Set.of()
    ERROR(ImmutableSet.of(CallbackFields.ERROR, DESCRIPTION, STATE)),
    MULTIPLE_MATCH(ImmutableSet.of()),
    UNKNOWN(ImmutableSet.of());

    private final Set<String> expectedProperties;

    CmcicCallbackStatus(Set<String> expectedProperties) {
        this.expectedProperties = expectedProperties;
    }

    public static CmcicCallbackStatus extract(Set<String> propertiesToCheck) {
        CmcicCallbackStatus returnStatus = null;
        for (CmcicCallbackStatus status : CmcicCallbackStatus.values()) {
            if (!status.expectedProperties.isEmpty()
                    && propertiesToCheck.containsAll(status.expectedProperties)) {
                if (returnStatus == null) {
                    returnStatus = status;
                } else {
                    returnStatus = MULTIPLE_MATCH;
                    break;
                }
            }
        }
        return returnStatus != null ? returnStatus : UNKNOWN;
    }

    public Set<String> getExpectedProperties() {
        return expectedProperties;
    }
}
