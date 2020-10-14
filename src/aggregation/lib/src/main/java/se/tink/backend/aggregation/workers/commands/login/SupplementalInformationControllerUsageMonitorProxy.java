package se.tink.backend.aggregation.workers.commands.login;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public class SupplementalInformationControllerUsageMonitorProxy
        implements SupplementalInformationController {

    private final SupplementalInformationController proxifiedSupplementalInformationController;
    private boolean used;

    public SupplementalInformationControllerUsageMonitorProxy(
            SupplementalInformationController proxifiedSupplementalInformationController) {
        this.proxifiedSupplementalInformationController =
                proxifiedSupplementalInformationController;
    }

    @Override
    public Optional<Map<String, String>> waitForSupplementalInformation(
            String key, long waitFor, TimeUnit unit) {
        used = true;
        return proxifiedSupplementalInformationController.waitForSupplementalInformation(
                key, waitFor, unit);
    }

    @Override
    public Map<String, String> askSupplementalInformation(Field... fields)
            throws SupplementalInfoException {
        used = true;
        return proxifiedSupplementalInformationController.askSupplementalInformation(fields);
    }

    @Override
    public void openThirdPartyApp(ThirdPartyAppAuthenticationPayload payload) {
        used = true;
        proxifiedSupplementalInformationController.openThirdPartyApp(payload);
    }

    public boolean isUsed() {
        return used;
    }
}
