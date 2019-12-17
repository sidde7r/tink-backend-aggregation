package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities.IbanEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.utils.RangeRegex;
import se.tink.libraries.i18n.Catalog;

public class ArgentaAuthenticator implements OAuth2Authenticator {

    private final Credentials credentials;
    private final ArgentaApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;

    public ArgentaAuthenticator(
            Credentials credentials,
            ArgentaApiClient apiClient,
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            Catalog catalog) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.catalog = catalog;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        List<IbanEntity> ibans =
                Stream.of(credentials.getField(CredentialKeys.IBAN).split(","))
                        .map(String::trim)
                        .map(IbanEntity::new)
                        .collect(Collectors.toList());

        ConsentResponse consentResponse = apiClient.getConsent(ibans);
        selectScaMethod(consentResponse);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        return apiClient.buildAuthorizeUrl(state, consentResponse.getConsentId());
    }

    private void selectScaMethod(ConsentResponse consentResponse) {
        List<ScaMethodEntity> scaMethods = consentResponse.getScaMethods();

        // Select SCA method when user has more than one device.
        if (CollectionUtils.isNotEmpty(scaMethods)) {

            Map<String, String> supplementalInformation;

            try {
                supplementalInformation =
                        this.supplementalInformationHelper.askSupplementalInformation(
                                this.getChosenScaMethod(scaMethods));
            } catch (SupplementalInfoException e) {
                throw new IllegalStateException(e.getMessage());
            }

            int index =
                    Integer.valueOf(supplementalInformation.get(QueryKeys.CHOSEN_SCA_METHOD)) - 1;
            ScaMethodEntity scaMethodEntity = scaMethods.get(index);
            String selectAuthenticationMethodUrl =
                    consentResponse.getSelectAuthenticationMethodUrl();

            apiClient.selectAuthenticationMethod(
                    selectAuthenticationMethodUrl, scaMethodEntity.getAuthenticationMethodId());
        }
    }

    private Field getChosenScaMethod(List<ScaMethodEntity> scaMethods) {
        int maxNumber = scaMethods.size();
        int length = Integer.toString(maxNumber).length();
        String description =
                IntStream.range(0, maxNumber)
                        .mapToObj(
                                i -> String.format("(%d) %s", i + 1, scaMethods.get(i).toString()))
                        .collect(Collectors.joining("\n"));
        String regexForRangePattern = RangeRegex.regexForRange(1, maxNumber);

        return Field.builder()
                .description("Enter number of selected SCA method")
                .helpText("SCA methods:\n" + this.catalog.getString(description))
                .name(QueryKeys.CHOSEN_SCA_METHOD)
                .numeric(true)
                .minLength(1)
                .maxLength(length)
                .hint(String.format("Select from 1 to %d", maxNumber))
                .pattern(regexForRangePattern)
                .patternError(ArgentaConstants.ErrorMessages.INVALID_SCA_METHOD)
                .build();
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return apiClient.exchangeAuthorizationCode(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        return apiClient.exchangeRefreshToken(refreshToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
