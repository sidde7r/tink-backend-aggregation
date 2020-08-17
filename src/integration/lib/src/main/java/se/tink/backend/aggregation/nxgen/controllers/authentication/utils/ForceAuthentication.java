package se.tink.backend.aggregation.nxgen.controllers.authentication.utils;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;

public class ForceAuthentication {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static boolean shouldForceAuthentication(CredentialsRequest request) {
        boolean shouldForceAuthentication =
                request instanceof RefreshInformationRequest
                        && ((RefreshInformationRequest) request).isForceAuthenticate();
        logger.info(
                "[forceAuthenticate] Should force authentication for credentials: {}, {}",
                request.getCredentials().getId(),
                shouldForceAuthentication);
        return shouldForceAuthentication;
    }
}
