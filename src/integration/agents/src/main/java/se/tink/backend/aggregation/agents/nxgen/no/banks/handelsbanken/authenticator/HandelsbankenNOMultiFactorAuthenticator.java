package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.ActivationCodeFieldConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.Tags;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FinalizeBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FirstLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.FirstLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.SendSmsRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc.VerifyCustomerResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticatorNO;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
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

        apiClient.fetchAppInformation();

        VerifyCustomerResponse response = apiClient.verifyCustomer(nationalId, this.mobileNumber);
        if (!response.isValid()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        apiClient.configureBankId(nationalId, this.mobileNumber);

        InitBankIdRequest initBankIdRequest = InitBankIdRequest.build(dob, this.mobileNumber);
        String initBankIdResponse = apiClient.initBankId(initBankIdRequest);

        Element referenceWords =
                Jsoup.parse(initBankIdResponse).getElementsByClass(Tags.REFERENCE_WORD).first();

        if (referenceWords == null) {
            throw new IllegalStateException("HB_bankID: No reference words found");
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
            executeLogin(getActivateEvryToken());
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

    private String getBankIdEvryToken() {
        FinalizeBankIdRequest finalizeBankIdRequest = FinalizeBankIdRequest.build();
        String finalizedBankIdResponse = apiClient.finalizeBankId(finalizeBankIdRequest);
        String evryToken =
                Jsoup.parse(finalizedBankIdResponse)
                        .getElementsByAttributeValue(Tags.NAME, Tags.EVRY_TOKEN_FIELD_VALUE)
                        .first()
                        .val();

        if (evryToken == null) {
            throw new IllegalStateException(
                    "can not retrieve every token, could it change field key? :"
                            + finalizedBankIdResponse);
        }
        return evryToken;
    }

    private String getActivateEvryToken() throws SupplementalInfoException, LoginException {

        SendSmsRequest sendSmsRequest = SendSmsRequest.build(this.mobileNumber);
        HttpResponse smsResponse = apiClient.sendSms(sendSmsRequest);

        if (smsResponse.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            throw new IllegalStateException(
                    "Handelsbanken No - SMS not send : " + smsResponse.getStatus());
        }

        Map<String, String> activationCodeResponse =
                supplementalInformationController.askSupplementalInformation(
                        getActivationCodeField());

        String activateEvryToken =
                encapClient.activateAndAuthenticateUser(
                        activationCodeResponse.get(ActivationCodeFieldConstants.NAME));

        if (activateEvryToken == null) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
        }
        return activateEvryToken;
    }

    private void executeLogin(String evryToken) {
        String firstLoginRequest = FirstLoginRequest.build(evryToken);
        FirstLoginResponse firstLoginResponse = apiClient.loginFirstStep(firstLoginRequest);
        sessionStorage.put(Tags.ACCESS_TOKEN, firstLoginResponse.getAccessToken());
        apiClient.loginSecondStep();
    }

    private Field getActivationCodeField() {
        Field activationCodeField = new Field();
        activationCodeField.setDescription(
                catalog.getString(ActivationCodeFieldConstants.DESCRIPTION));
        activationCodeField.setName(ActivationCodeFieldConstants.NAME);
        activationCodeField.setNumeric(true);
        activationCodeField.setMinLength(ActivationCodeFieldConstants.LENGTH);
        activationCodeField.setMaxLength(ActivationCodeFieldConstants.LENGTH);
        activationCodeField.setHint(StringUtils.repeat("N", ActivationCodeFieldConstants.LENGTH));
        activationCodeField.setPattern(
                String.format("([0-9]{%d})", ActivationCodeFieldConstants.LENGTH));
        activationCodeField.setPatternError(ActivationCodeFieldConstants.PATTERN_ERROR);

        return activationCodeField;
    }
}
