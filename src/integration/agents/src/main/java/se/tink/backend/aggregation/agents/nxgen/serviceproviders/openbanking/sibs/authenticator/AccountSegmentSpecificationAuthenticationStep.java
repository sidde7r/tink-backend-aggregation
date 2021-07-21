package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import java.util.Optional;
import java.util.Set;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.FinancialService;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SingleSupplementalFieldAuthenticationStep;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.HasRefreshScope;

public class AccountSegmentSpecificationAuthenticationStep
        extends SingleSupplementalFieldAuthenticationStep {

    private final SibsUserState userState;
    private final CredentialsRequest credentialsRequest;

    public AccountSegmentSpecificationAuthenticationStep(
            SibsUserState userState,
            CredentialsRequest credentialsRequest,
            Field accountSegmentField) {
        super(
                "accountSegmentStep",
                value -> {
                    userState.specifyAccountSegment(SibsAccountSegment.getSegment(value));
                    return AuthenticationStepResponse.executeNextStep();
                },
                accountSegmentField);
        this.userState = userState;
        this.credentialsRequest = credentialsRequest;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        return tryFetchAccountSegmentFromCredentialsRequest()
                .orElseGet(
                        () ->
                                requestUserFormAccountSegmentChoiceInNeeded(request)
                                        .orElse(AuthenticationStepResponse.executeNextStep()));
    }

    private Optional<AuthenticationStepResponse> tryFetchAccountSegmentFromCredentialsRequest() {
        if (userState.isAccountSegmentNotSpecified()
                && credentialsRequest instanceof HasRefreshScope
                && ((HasRefreshScope) credentialsRequest).getRefreshScope() != null) {
            Set<FinancialService.FinancialServiceSegment> financialServiceSegments =
                    ((HasRefreshScope) credentialsRequest)
                            .getRefreshScope()
                            .getFinancialServiceSegmentsIn();
            if (financialServiceSegments.contains(FinancialService.FinancialServiceSegment.BUSINESS)
                    && !financialServiceSegments.contains(
                            FinancialService.FinancialServiceSegment.PERSONAL)) {
                userState.specifyAccountSegment(SibsAccountSegment.BUSINESS);
                return Optional.of(AuthenticationStepResponse.executeNextStep());
            } else if (financialServiceSegments.contains(
                            FinancialService.FinancialServiceSegment.PERSONAL)
                    && !financialServiceSegments.contains(
                            FinancialService.FinancialServiceSegment.BUSINESS)) {
                userState.specifyAccountSegment(SibsAccountSegment.PERSONAL);
                return Optional.of(AuthenticationStepResponse.executeNextStep());
            }
        }
        return Optional.empty();
    }

    private Optional<AuthenticationStepResponse> requestUserFormAccountSegmentChoiceInNeeded(
            AuthenticationRequest request) {
        if (userState.isAccountSegmentNotSpecified() && !userState.hasConsentId()) {
            return Optional.of(super.execute(request));
        }
        return Optional.empty();
    }
}
