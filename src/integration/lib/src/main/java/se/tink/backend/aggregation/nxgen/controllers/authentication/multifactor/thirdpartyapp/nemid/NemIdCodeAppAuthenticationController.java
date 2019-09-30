package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class NemIdCodeAppAuthenticationController
        extends ThirdPartyAppAuthenticationController<String> {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public NemIdCodeAppAuthenticationController(
            NemIdCodeAppAuthenticator authenticator,
            SupplementalInformationHelper supplementalInformationHelper) {
        super(authenticator, supplementalInformationHelper);
    }
}
