package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;

@Slf4j
public class ScaMethodFilter {

    private static final String PUSH_OTP = "PUSH_OTP";
    private static final String PUSH_DEC = "PUSH_DEC";

    private static final ImmutableList<String> UNSUPPORTED_AUTH_IDS =
            ImmutableList.of("OPTICAL", "QR");

    public List<ScaMethodEntity> getUsableScaMethods(List<ScaMethodEntity> allScaMethods) {
        List<ScaMethodEntity> supportedMethods = getOnlySupported(allScaMethods);
        return removePushOtpIfDecoupledAvailable(supportedMethods);
    }

    private List<ScaMethodEntity> getOnlySupported(List<ScaMethodEntity> allScaMethods) {
        return allScaMethods.stream()
                .filter(
                        scaMethod ->
                                !UNSUPPORTED_AUTH_IDS.contains(
                                        scaMethod.getAuthenticationMethodId()))
                .collect(Collectors.toList());
    }

    private List<ScaMethodEntity> removePushOtpIfDecoupledAvailable(
            List<ScaMethodEntity> supportedMethods) {
        // Focus only on PUSH_DEC and PUSH_OTP, group by name, since it is the same for both methods
        Map<String, List<ScaMethodEntity>> groups =
                supportedMethods.stream()
                        .filter(
                                x ->
                                        PUSH_DEC.equalsIgnoreCase(x.getAuthenticationType())
                                                || PUSH_OTP.equalsIgnoreCase(
                                                        x.getAuthenticationType()))
                        .collect(Collectors.groupingBy(ScaMethodEntity::getName));

        List<String> methodIdsToSkip = new ArrayList<>();
        // We are removing only when we are sure that the group has exactly two elements, with
        // different method types
        // This should always be the case, so loging weird situations for observations!
        for (Map.Entry<String, List<ScaMethodEntity>> entry : groups.entrySet()) {
            String name = entry.getKey();
            List<ScaMethodEntity> methodsForName = entry.getValue();

            if (methodsForName.size() > 2) {
                log.info(
                        "There are more than expected two sca methods of PUSH_DEC/PUSH_OTP type with the same name! The methods sharing this name will be left as-is. Name - {}",
                        name);
            } else if (methodsForName.size() == 2) {
                ScaMethodEntity first = methodsForName.get(0);
                ScaMethodEntity second = methodsForName.get(1);

                // Make sure that method types are different!
                if (first.getAuthenticationType().equals(second.getAuthenticationType())) {
                    log.info(
                            "Unexpected auth types on pair with the same name. Name - {}, authTypes - {}, {}",
                            name,
                            first.getAuthenticationType(),
                            second.getAuthenticationType());
                } else {
                    methodIdsToSkip.add(
                            PUSH_OTP.equalsIgnoreCase(first.getAuthenticationType())
                                    ? first.getAuthenticationMethodId()
                                    : second.getAuthenticationMethodId());
                }
            }
        }

        if (!methodIdsToSkip.isEmpty()) {
            log.info(
                    "PUSH_OTP methods that can be skipped found! Amount: {}",
                    methodIdsToSkip.size());
        }

        return supportedMethods.stream()
                .filter(x -> !methodIdsToSkip.contains(x.getAuthenticationMethodId()))
                .collect(Collectors.toList());
    }
}
