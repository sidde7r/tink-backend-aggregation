package se.tink.backend.aggregation.nxgen.controllers.authentication.utils;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ForceAuthentication {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static boolean shouldForceAuthentication(CredentialsRequest request) {
        if (request == null) {
            return false; // payments reuse lots of aggregation functionality and they do not
            // populate credentials request
        }
        boolean shouldForceAuthentication =
                (request instanceof CredentialsRequest && request.isForceAuthenticate());

        logger.info(
                "[forceAuthenticate] Should force authentication for credentials: {}, {}",
                request.getCredentials().getId(),
                shouldForceAuthentication);
        return shouldForceAuthentication;
    }
}
