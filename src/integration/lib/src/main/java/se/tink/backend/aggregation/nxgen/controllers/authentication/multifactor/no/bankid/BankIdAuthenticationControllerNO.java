package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.no.BankIdErrorNO;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AuthenticationControllerType;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BankIdAuthenticationControllerNO
        implements TypedAuthenticator, AuthenticationControllerType {
    private static final int MAX_ATTEMPTS = 90;

    private static final AggregationLogger log =
            new AggregationLogger(BankIdAuthenticationControllerNO.class);
    private final BankIdAuthenticatorNO authenticator;
    private final SupplementalRequester supplementalRequester;

    public BankIdAuthenticationControllerNO(
            SupplementalRequester supplementalRequester, BankIdAuthenticatorNO authenticator) {
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.supplementalRequester = Preconditions.checkNotNull(supplementalRequester);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.MOBILE_BANKID;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));

        String nationalId = credentials.getField(Field.Key.USERNAME);
        String mobilenumber = credentials.getField(Field.Key.MOBILENUMBER);

        if (Strings.isNullOrEmpty(nationalId) || Strings.isNullOrEmpty(mobilenumber)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        String dob = nationalId.substring(0, 6);
        String bankIdReference = authenticator.init(nationalId, dob, mobilenumber);
        displayBankIdReference(credentials, bankIdReference);
        poll();
    }

    private void displayBankIdReference(Credentials credentials, String bankIdReference) {
        Field field =
                Field.builder()
                        .name("name")
                        .immutable(true)
                        .description("Reference")
                        .value(bankIdReference)
                        .helpText(
                                "Continue by clicking update when you have verified the reference and signed with Mobile BankID.")
                        .build();

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Lists.newArrayList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        supplementalRequester.requestSupplementalInformation(credentials, false);
    }

    public void poll() throws AuthenticationException, AuthorizationException {
        BankIdStatus status = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            status = authenticator.collect();

            switch (status) {
                case DONE:
                    return;
                case WAITING:
                    log.info("Waiting for BankID");
                    break;
                case CANCELLED:
                    throw BankIdErrorNO.CANCELLED.exception();
                case TIMEOUT:
                    throw BankIdErrorNO.TIMEOUT.exception();
                default:
                    log.warn(String.format("Unknown Norweigan BankIdStatus (%s)", status));
                    throw BankIdErrorNO.UNKNOWN.exception();
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        log.info(String.format("Norweigan BankID timed out internally, last status: %s", status));
        throw BankIdErrorNO.TIMEOUT.exception();
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        // since authenticate always init bankid
        return true;
    }
}
