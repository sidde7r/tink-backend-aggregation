package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.lang3.CharSet;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities.AuthResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities.EBankingUserIdEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities.ValueEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.rpc.AuthenticationProcessRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.rpc.EBankingUsersRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.rpc.GenerateChallangeRequest;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class FortisAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private final Catalog catalog;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final FortisApiClient apiClient;
    private final SupplementalInformationController supplementalInformationController;

    public FortisAuthenticator(Catalog catalog, PersistentStorage persistentStorage, FortisApiClient apiClient,
            SupplementalInformationController supplementalInformationController, SessionStorage sessionStorage) {
        this.catalog = catalog;
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {

        sessionStorage.put("AUTHENTICATION_FACTOR_ID", "67030416073389570");
        sessionStorage.put("DISTRIBUTOR_ID", "49FB001");
        EBankingUsersRequest eBankingUsersRequest = new EBankingUsersRequest(sessionStorage.get("AUTHENTICATION_FACTOR_ID"), sessionStorage.get("DISTRIBUTOR_ID"), "1880625810");
        try {
            ValueEntity valueEntity = apiClient.getEBankingUsers(eBankingUsersRequest);
            EBankingUserIdEntity eBankingUserIdEntity = valueEntity.geteBankingUsers().get(0).geteBankingUser().geteBankingUserId();
            sessionStorage.put("SMID", eBankingUserIdEntity.getSmid());
            sessionStorage.put("AGREEMENT_ID", eBankingUserIdEntity.getAgreementId());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        HttpResponse response = null;
        EBankingUserIdEntity eBankingUserIdEntity = new EBankingUserIdEntity("","1880625810", "E3749089", "");
        AuthenticationProcessRequest authenticationProcessRequest = new AuthenticationProcessRequest(eBankingUserIdEntity, "49FB001", "08");
        try {
            response = apiClient.createAuthenticationProcess(authenticationProcessRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        sessionStorage.put("AUTHENTICATION_PROCESS_ID", response.getBody(ValueEntity.class).getAuthenticationProcessId());
        GenerateChallangeRequest challangeRequest = new GenerateChallangeRequest("49FB001", sessionStorage.get("AUTHENTICATION_PROCESS_ID"));
        try {
            String challenge = apiClient.generateChallanges(challangeRequest);
            String loginCode = waitForLoginCode(challenge);
            AuthResponse authResponse = AuthResponse.builder().withAuthProcId(sessionStorage.get("AUTHENTICATION_PROCESS_ID"))
                    .withAgreementId(sessionStorage.get("AGREEMENT_ID"))
                    .withAuthenticationMeanId("MEAN_ID")
                    .withCardNumber("AUTHENTICATION_FACTOR_ID")
                    .withDistId("DISTRIBUTOR_ID")
                    .withSmid("SMID")
                    .withChallenge(challenge)
                    .withResponse(loginCode)
                    .build();

            String authResponseBody = AuthResponse.xmlBuilder(authResponse);
            String authDoubleEncodedBody = URLEncoder.encode(URLEncoder.encode(authResponseBody, "UTF-8"), "UTF-8");
            apiClient.authenticationRequest(authDoubleEncodedBody);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void autoAuthenticate() throws SessionException {

    }

    private String waitForLoginCode(String challenge) throws SupplementalInfoException {
    return waitForSupplementalInformation(
        createDescriptionField(catalog.getString(
                "1. Insert your card into the card reader and press (M1)\n"
                + "2. 'CHALLENGE?' is displayed."
                + "Enter []"),
            challenge),
        createInputField(catalog.getString(
                "3. 'PIN?' is displayed.\n"
                    + "Enter your PIN and press (OK)\n"
                    + "4. The e-signature is displayed.\n"
                    + "Enter the e-signature")));
    }

    private String waitForSupplementalInformation(Field... fields)
            throws SupplementalInfoException {
        return supplementalInformationController.askSupplementalInformation(fields)
                .get("e-signature");
    }

    private Field createDescriptionField(String loginText, String challenge) {
//        String formattedChallenge = getChallengeFormattedWithSpace(challenge);
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(challenge);
        field.setValue(challenge);
        field.setName("description");
        field.setHelpText(loginText);
        field.setImmutable(true);
        return field;
    }

    private Field createInputField(String loginText) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(catalog.getString("Input"));
        field.setName("e-signature");
        field.setHelpText(loginText);
        field.setNumeric(true);
        return field;
    }

    private String getChallengeFormattedWithSpace(String challenge) {
        if (challenge.length() != 8) {
            // We expect the challenge to consist of 8 numbers, if not we don't try to format for readability
            return challenge;
        }

        return String.format("%s %s",
                challenge.substring(0, 4),
                challenge.substring(4));
    }
}
