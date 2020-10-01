package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n.Catalog;

// Controller to be used together with Authenticator extending NemIdCodeAppAuthenticator
// It is just like ThirdPartyApp, but due to not being able to redirect to NemIdCodeApp properly, it
// was changed to show a message to user to do app switching manually.
public class NemIdCodeAppAuthenticationController
        extends ThirdPartyAppAuthenticationController<String> {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Catalog catalog;

    public NemIdCodeAppAuthenticationController(
            NemIdCodeAppAuthenticator authenticator,
            SupplementalInformationHelper supplementalInformationHelper,
            Catalog catalog) {
        super(authenticator, supplementalInformationHelper);
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
        Field field =
                Field.builder()
                        .immutable(true)
                        .description(
                                catalog.getString(
                                        NemIdCodeAppConstants.UserMessage.OPEN_NEM_ID_APP))
                        .value(catalog.getString(NemIdCodeAppConstants.UserMessage.OPEN_NEM_ID_APP))
                        .name("name")
                        .build();

        supplementalInformationHelper.askSupplementalInformation(field);
    }
}
