package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public interface SupplementalInformationController {

    Optional<Map<String, String>> waitForSupplementalInformation(
            String key, long waitFor, TimeUnit unit);

    Map<String, String> askSupplementalInformation(Field... fields)
            throws SupplementalInfoException;

    void openThirdPartyApp(ThirdPartyAppAuthenticationPayload payload);
}
