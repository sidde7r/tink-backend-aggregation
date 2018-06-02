package se.tink.backend.aggregation.nxgen.controllers.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SupplementalInformationController {

    private final AgentContext context;
    private final Credentials credentials;

    public SupplementalInformationController(AgentContext context, Credentials credentials) {
        this.context = context;
        this.credentials = credentials;
    }

    public Map<String, String> askSupplementalInformation(Field... fields) throws SupplementalInfoException {
        CredentialsStatus oldStatus = credentials.getStatus();
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));
        String supplementalInformation =
                Optional.ofNullable(Strings.emptyToNull(context.requestSupplementalInformation(credentials)))
                        .orElseThrow(SupplementalInfoError.NO_VALID_CODE::exception);

        // we must reset the credential status in order to request supplemental info multiple times
        credentials.setStatus(oldStatus);
        context.updateStatus(oldStatus);

        return Optional.ofNullable(
                SerializationUtils.deserializeFromString(
                        supplementalInformation, new TypeReference<HashMap<String, String>>() {
                        }
                ))
                .orElseThrow(() -> new IllegalStateException("SupplementalInformationResponse cannot be deserialized"));
    }
}
