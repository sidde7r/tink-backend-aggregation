package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.ActivationCodeFieldConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.Tags;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FinalizeBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FirstLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FirstLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.SendSmsRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.VerifyCustomerResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticatorNO;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class HandelsbankenNOMultiFactorAuthenticator implements BankIdAuthenticatorNO {
    private final HandelsbankenNOApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;
    private final EncapClient encapClient;

    public final Logger log =
            LoggerFactory.getLogger(HandelsbankenNOMultiFactorAuthenticator.class);

    private int pollWaitCounter;
    private String mobileNumber;
    private String nationalId;

    public HandelsbankenNOMultiFactorAuthenticator(
            HandelsbankenNOApiClient apiClient,
            SessionStorage sessionStorage,
            SupplementalInformationController supplementalInformationController,
            Catalog catalog,
            EncapClient encapClient) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.supplementalInformationController = supplementalInformationController;
        this.catalog = catalog;
        this.encapClient = encapClient;
    }

    @Override
    public String init(String nationalId, String dob, String mobileNumber)
            throws AuthenticationException {
        pollWaitCounter = 0;
        this.mobileNumber = mobileNumber;
        this.nationalId = nationalId;

        apiClient.fetchAppInformation();

        VerifyCustomerResponse response = apiClient.verifyCustomer(nationalId, this.mobileNumber);
        if (!response.isValid()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        apiClient.configureBankId(nationalId, this.mobileNumber);

        InitBankIdRequest initBankIdRequest = InitBankIdRequest.build(dob, this.mobileNumber);
        String initBankIdResponse = apiClient.initBankId(initBankIdRequest);

        Document parsedDocument = Jsoup.parse(initBankIdResponse);

        Element referenceWords = parsedDocument.getElementsByClass(Tags.REFERENCE_WORD).first();

        if (referenceWords == null) {
            handleReferenceWordsError(parsedDocument);
        }

        return referenceWords.text();
    }

    @Override
    public BankIdStatus collect() throws AuthenticationException {

        PollBankIdResponse pollBankIdResponse = apiClient.pollBankId();
        String pollStatus = pollBankIdResponse.getStatus();

        if (pollStatus.equalsIgnoreCase(
                HandelsbankenNOConstants.BankIdAuthenticationStatus.COMPLETE)) {
            executeLogin(getBankIdEvryToken());
            return BankIdStatus.DONE;
        } else if (pollStatus.equalsIgnoreCase(
                HandelsbankenNOConstants.BankIdAuthenticationStatus.NONE)) {
            pollWaitCounter++;
            return BankIdStatus.WAITING;
        } else if (pollStatus.equalsIgnoreCase(
                HandelsbankenNOConstants.BankIdAuthenticationStatus.ERROR)) {
            if (pollWaitCounter > HandelsbankenNOConstants.AUTHENTICATION_TIMEOUT_COUNT) {
                return BankIdStatus.TIMEOUT;
            }
        }

        log.error(
                "unexpected state when polling for bank ID: "
                        + SerializationUtils.serializeToString(pollBankIdResponse));
        return BankIdStatus.FAILED_UNKNOWN;
    }

    @Override
    public void sendActivationCode() throws SupplementalInfoException {
        executeLogin(getActivateEvryToken());
    }

    private void handleReferenceWordsError(Document parsedDocument) {
        Elements errorElements = parsedDocument.getElementsByClass(Tags.LOGIN_ERROR);
        if (!errorElements.isEmpty()) {
            String errorData = "";
            for (Element element : errorElements) {
                errorData = element.text();
                log.info("Handelsbanken No - Error message: {}", errorData);
                if (errorData.contains(
                        ErrorCode.WRONG_PHONE_NUMBER_OR_INACTIVATED_SERVICE_ERROR_CODE)) {
                    throw LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE.exception();
                }
            }
            log.error("Handelsbanken No - Unknown error message: {}", errorData);
            throw LoginError.DEFAULT_MESSAGE.exception(errorData);
        } else {
            throw new IllegalStateException("Unknown error code when missing reference words");
        }
    }

    private String getBankIdEvryToken() {
        FinalizeBankIdRequest finalizeBankIdRequest = FinalizeBankIdRequest.build();
        String finalizedBankIdResponse = apiClient.finalizeBankId(finalizeBankIdRequest);
        Document doc = Jsoup.parse(finalizedBankIdResponse);
        try {
            Element scriptTag = doc.getElementsByTag("script").get(0);
            String scriptData = scriptTag.dataNodes().get(0).getWholeData();
            String evryToken = StringUtils.substringBetween(scriptData, "?so=", "&");
            sessionStorage.put(Storage.EVRY_TOKEN, evryToken);
            return evryToken;
        } catch (NullPointerException npe) {
            throw new IllegalStateException(
                    "can not retrieve every token, could it change field key? :"
                            + finalizedBankIdResponse);
        }
    }

    private String getActivateEvryToken() throws SupplementalInfoException, LoginException {

        SendSmsRequest sendSmsRequest = SendSmsRequest.build(this.mobileNumber);
        HttpResponse smsResponse = apiClient.sendSms(sendSmsRequest);

        if (smsResponse.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            throw new IllegalStateException(
                    "Handelsbanken No - SMS not send : " + smsResponse.getStatus());
        }

        Map<String, String> activationCodeResponse =
                supplementalInformationController.askSupplementalInformationSync(
                        getActivationCodeField());

        if (activationCodeResponse.get(ActivationCodeFieldConstants.NAME) == null) {
            log.info(
                    "Activation code was not retrieved. Possible options in response: [{}]",
                    activationCodeResponse);
        }

        try {
            String activateEvryToken =
                    encapClient
                            .registerDevice(
                                    nationalId,
                                    activationCodeResponse.get(ActivationCodeFieldConstants.NAME))
                            .getDeviceToken();

            sessionStorage.put(Storage.ACTIVATE_EVRY_TOKEN, activateEvryToken);
            if (activateEvryToken == null) {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
            }
            return activateEvryToken;
        } finally {
            encapClient.saveDevice();
        }
    }

    private void executeLogin(String evryToken) {
        FirstLoginRequest firstLoginRequest = FirstLoginRequest.build(evryToken);
        FirstLoginResponse firstLoginResponse = apiClient.loginFirstStep(firstLoginRequest);
        sessionStorage.put(Tags.ACCESS_TOKEN, firstLoginResponse.getAccessToken());
        apiClient.loginSecondStep();
    }

    private Field getActivationCodeField() {
        return Field.builder()
                .description(catalog.getString(ActivationCodeFieldConstants.DESCRIPTION))
                .name(ActivationCodeFieldConstants.NAME)
                .numeric(true)
                .minLength(ActivationCodeFieldConstants.LENGTH)
                .maxLength(ActivationCodeFieldConstants.LENGTH)
                .hint(StringUtils.repeat("N", ActivationCodeFieldConstants.LENGTH))
                .pattern(String.format("([0-9]{%d})", ActivationCodeFieldConstants.LENGTH))
                .patternError(ActivationCodeFieldConstants.PATTERN_ERROR)
                .build();
    }
}
