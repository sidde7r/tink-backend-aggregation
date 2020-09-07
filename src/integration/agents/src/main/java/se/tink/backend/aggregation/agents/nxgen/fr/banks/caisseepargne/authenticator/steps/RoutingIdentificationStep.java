package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.steps;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.IdentificationRoutingResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.storage.CaisseEpargneStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities.MembershipType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@RequiredArgsConstructor
public class RoutingIdentificationStep extends AbstractAuthenticationStep {

    public static final String STEP_ID = "routingIdentificationStep";

    private final CaisseEpargneApiClient caisseEpargneApiClient;
    private final CaisseEpargneStorage caisseEpargneStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final String username =
                Strings.emptyToNull(request.getCredentials().getField(Field.Key.USERNAME));
        Preconditions.checkNotNull(username);

        caisseEpargneApiClient.getOAuth2Token().ifPresent(token -> retrieveBankId(username, token));

        return AuthenticationStepResponse.executeNextStep();
    }

    @Override
    public String getIdentifier() {
        return STEP_ID;
    }

    private void retrieveBankId(String username, OAuth2Token oAuth2Token) {
        final IdentificationRoutingResponse identificationRoutingResponse =
                caisseEpargneApiClient.identificationRouting(username, oAuth2Token);

        if (!identificationRoutingResponse.isValid()) {
            throw new IllegalArgumentException("Invalid routing response");
        }

        final String bankId = identificationRoutingResponse.getBankId();
        final MembershipType membershipType =
                MembershipType.fromString(identificationRoutingResponse.getMembershipTypeCode());

        caisseEpargneStorage.storeBankId(bankId);
        caisseEpargneStorage.storeMembershipType(membershipType);
        caisseEpargneStorage.storeIdRoutingResponse(identificationRoutingResponse);
        caisseEpargneStorage.storeIdRoutingOAuth2Token(oAuth2Token);
    }
}
