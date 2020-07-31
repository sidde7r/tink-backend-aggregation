package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngCardReaderAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.ProgressiveTypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public final class IngCardReaderAuthenticationController implements ProgressiveTypedAuthenticator {

    private final IngCardReaderAuthenticator authenticator;
    private final SupplementalInformationFormer supplementalInformationFormer;

    public IngCardReaderAuthenticationController(
            IngCardReaderAuthenticator authenticator,
            SupplementalInformationFormer supplementalInformationFormer) {
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.supplementalInformationFormer =
                Preconditions.checkNotNull(supplementalInformationFormer);
    }

    @Override
    public Iterable<AuthenticationStep> authenticationSteps() {
        return Arrays.asList(
                new OtpStep(supplementalInformationFormer),
                new SignStep(supplementalInformationFormer, authenticator),
                new FinalStep(authenticator));
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
