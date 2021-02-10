package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.utils.supplementalfields.DanishFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

// Controller to be used together with Authenticator extending NemIdCodeAppAuthenticator
// It is just like ThirdPartyApp, but due to not being able to redirect to NemIdCodeApp properly, it
// was changed to show a message to user to do app switching manually.
public class NemIdCodeAppAuthenticationController
        extends ThirdPartyAppAuthenticationController<String> {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Credentials credentials;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    public NemIdCodeAppAuthenticationController(
            NemIdCodeAppAuthenticator authenticator,
            Credentials credentials,
            SupplementalInformationController supplementalInformationController,
            Catalog catalog) {
        super(authenticator, null);
        this.credentials = credentials;
        this.supplementalInformationController = supplementalInformationController;
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

        try {
            supplementalInformationController.askSupplementalInformationSync(field);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }
}
