package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.steps;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.BanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.AccessTokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.AppConfigDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.BankResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.identity.UserIdentityDataDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.storage.BanquePopulaireStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.AuthTransactionResponseDto;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@Slf4j
@RequiredArgsConstructor
public class FinalizeAuthStep extends AbstractAuthenticationStep {
    public static final String STEP_ID = "finalizeAuthStep";
    private final BanquePopulaireApiClient banquePopulaireApiClient;
    private final BanquePopulaireStorage banquePopulaireStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        final String username = request.getCredentials().getField(Field.Key.USERNAME);
        final BankResourceDto bankResourceDto = banquePopulaireStorage.getBankResource();
        final AppConfigDto appConfigDto = banquePopulaireStorage.getAppConfig();

        final String authorizeSessionPath =
                banquePopulaireApiClient.startSession(username, appConfigDto, bankResourceDto);
        final String authTransactionPath =
                banquePopulaireApiClient.authorizeSession(authorizeSessionPath);
        final AuthTransactionResponseDto authTransactionResponseDto =
                banquePopulaireApiClient.getAuthTransactionForResources(authTransactionPath);

        validateAuthTransactionResponse(authTransactionResponseDto);

        banquePopulaireApiClient.sendAcsRequest(
                authTransactionResponseDto.getResponse().getSaml2Post());

        retrieveAndStoreUserIdentityData(username, appConfigDto, bankResourceDto);
        retrieveAndStoreAccessToken(appConfigDto);

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private void retrieveAndStoreUserIdentityData(
            String username, AppConfigDto appConfigDto, BankResourceDto bankResourceDto) {
        final UserIdentityDataDto userIdentityDataDto =
                banquePopulaireApiClient.getUserIdentityData(
                        username, appConfigDto, bankResourceDto);

        banquePopulaireStorage.storeUserIdentityDataDto(userIdentityDataDto);
    }

    private void retrieveAndStoreAccessToken(AppConfigDto appConfigDto) {
        final AccessTokenResponseDto accessTokenResponseDto =
                banquePopulaireApiClient.retrieveAuxAccessToken(appConfigDto);
        final OAuth2Token oAuth2Token =
                OAuth2Token.create(
                        accessTokenResponseDto.getTokenType(),
                        accessTokenResponseDto.getAccessToken(),
                        null,
                        accessTokenResponseDto.getExpiresIn());

        banquePopulaireStorage.storeAuxAccessToken(oAuth2Token);
    }

    private static void validateAuthTransactionResponse(
            AuthTransactionResponseDto authTransactionResponseDto) throws SessionException {
        if (Objects.isNull(authTransactionResponseDto.getResponse())
                || Objects.isNull(authTransactionResponseDto.getResponse().getSaml2Post())) {
            log.error("Got empty Saml Post response");
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
