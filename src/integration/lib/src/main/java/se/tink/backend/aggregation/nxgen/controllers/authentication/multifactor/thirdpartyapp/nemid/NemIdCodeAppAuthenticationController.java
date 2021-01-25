package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.utils.supplementalfields.DanishFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

// Controller to be used together with Authenticator extending NemIdCodeAppAuthenticator
// It is just like ThirdPartyApp, but due to not being able to redirect to NemIdCodeApp properly, it
// was changed to show a message to user to do app switching manually.
public class NemIdCodeAppAuthenticationController
        extends ThirdPartyAppAuthenticationController<String> {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Credentials credentials;
    private SupplementalRequester supplementalRequester;
    private Catalog catalog;

    public NemIdCodeAppAuthenticationController(
            NemIdCodeAppAuthenticator authenticator,
            Credentials credentials,
            SupplementalRequester supplementalRequester,
            Catalog catalog) {
        super(authenticator, null);
        this.credentials = credentials;
        this.supplementalRequester = supplementalRequester;
        this.catalog = catalog;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        ThirdPartyAppResponse<String> response = authenticator.init();

        displayMessageAndWait();
        handleStatus(response.getStatus());

        this.response = poll(response);
    }

    private void displayMessageAndWait() {
        Field field = DanishFields.NemIdInfo.build(catalog);

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Collections.singletonList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        supplementalRequester.requestSupplementalInformation(credentials, true);
    }
}
