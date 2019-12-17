package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.BancoPostaConstants.ErrorValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.BancoPostaConstants.UserMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.ScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.Catalog;

public class BancoPostaAuthenticationController extends CbiGlobeAuthenticationController {
    private static final String CHOSEN_SCA_METHOD = "chosenScaMethod";
    private final Catalog catalog;

    public BancoPostaAuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper,
            CbiGlobeAuthenticator authenticator,
            StrongAuthenticationState consentState,
            Catalog catalog) {
        super(supplementalInformationHelper, authenticator, consentState);
        this.catalog = catalog;
    }

    @Override
    public void accountConsentAuthentication() throws AuthenticationException {
        ConsentRequest consentRequest = authenticator.createConsentRequestAccount();

        ConsentScaResponse consentResponse =
                ((BancoPostaAuthenticator) authenticator)
                        .getConsentResponse(
                                ConsentType.ACCOUNT, consentRequest, consentState.getState());

        ScaMethodEntity chosenScaMethod = selectScaMethod(consentResponse.getScaMethods());

        URL authorizeUrl =
                ((BancoPostaAuthenticator) authenticator)
                        .buildAuthorizeUrl(consentResponse, chosenScaMethod);
        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl));
        waitForSupplementalInformation(ConsentType.ACCOUNT);
    }

    @Override
    public void transactionsConsentAuthentication(GetAccountsResponse getAccountsResponse)
            throws AuthenticationException {
        ConsentRequest consentRequest =
                authenticator.createConsentRequestBalancesTransactions(getAccountsResponse);

        ConsentScaResponse consentResponse =
                ((BancoPostaAuthenticator) authenticator)
                        .getConsentResponse(
                                ConsentType.BALANCE_TRANSACTION,
                                consentRequest,
                                consentState.getState());

        ScaMethodEntity chosenScaMethod = selectScaMethod(consentResponse.getScaMethods());

        URL authorizeUrl =
                ((BancoPostaAuthenticator) authenticator)
                        .buildAuthorizeUrl(consentResponse, chosenScaMethod);

        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl));
        waitForSupplementalInformation(ConsentType.BALANCE_TRANSACTION);
    }

    private ScaMethodEntity selectScaMethod(List<ScaMethodEntity> methods) {
        Map<String, String> supplementalInformation = getSupplementalInformation(methods);
        int index = Integer.valueOf(supplementalInformation.get(CHOSEN_SCA_METHOD)) - 1;
        return methods.get(index);
    }

    private Map<String, String> getSupplementalInformation(List<ScaMethodEntity> methods) {
        Map<String, String> supplementalInformation;
        try {
            supplementalInformation =
                    this.supplementalInformationHelper.askSupplementalInformation(
                            new Field[] {getChosenScaMethod(methods)});
        } catch (SupplementalInfoException e) {
            throw new IllegalStateException(ErrorValues.INVALID_CODE);
        }
        return supplementalInformation;
    }

    private Field getChosenScaMethod(List<ScaMethodEntity> scaMethods) {
        int maxNumber = scaMethods.size();
        String description =
                IntStream.range(0, maxNumber)
                        .mapToObj(
                                i -> String.format("(%d) %s", i + 1, scaMethods.get(i).toString()))
                        .collect(Collectors.joining(";\n"));

        return Field.builder()
                .description(UserMessages.INPUT_FIELD)
                .helpText(String.format(UserMessages.SELECT_INFO, maxNumber).concat(description))
                .name(CHOSEN_SCA_METHOD)
                .numeric(true)
                .minLength(1)
                .pattern(String.format("([1-%d])", maxNumber))
                .patternError(ErrorValues.INVALID_CODE_MESSAGE)
                .build();
    }
}
