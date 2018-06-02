package se.tink.backend.grpc.v1.converter.credential;

import java.util.Locale;
import org.junit.Test;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.grpc.v1.models.Credential;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.grpc.v1.models.Credential.Status.STATUS_AWAITING_MOBILE_BANKID_AUTHENTICATION;

public class CoreCredentialToGrpcCredentialConverterTest {
    private static final Locale DEFAULT_LOCALE = new Locale("sv_SE");

    @Test
    public void testConvertCredentialsWithAutoStartToken() {
        CoreCredentialToGrpcCredentialConverter converter = new CoreCredentialToGrpcCredentialConverter(DEFAULT_LOCALE);

        final String autoStartToken = "dummy-token";

        Credentials credentials = new Credentials();

        credentials.setId(UUIDUtils.generateUUID());
        credentials.setSupplementalInformation(autoStartToken);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        Credential result = converter.convertFrom(credentials);

        assertThat(result.getStatus()).isEqualTo(STATUS_AWAITING_MOBILE_BANKID_AUTHENTICATION);
        assertThat(result.getThirdPartyAppAuthentication()).isNotNull();
        assertThat(result.getThirdPartyAppAuthentication().getIos()).isNotNull();
        assertThat(result.getThirdPartyAppAuthentication().getAndroid()).isNotNull();

        // Some kind of verification
        assertThat(result.getThirdPartyAppAuthentication().getIos().getDeepLinkUrl()).contains(autoStartToken);
        assertThat(result.getThirdPartyAppAuthentication().getAndroid().getIntent()).contains(autoStartToken);
    }

    @Test
    public void testConvertCredentialsWithoutAutoStartToken() {
        CoreCredentialToGrpcCredentialConverter converter = new CoreCredentialToGrpcCredentialConverter(DEFAULT_LOCALE);

        Credentials credentials = new Credentials();

        credentials.setId(UUIDUtils.generateUUID());
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        Credential result = converter.convertFrom(credentials);

        assertThat(result.getStatus()).isEqualTo(STATUS_AWAITING_MOBILE_BANKID_AUTHENTICATION);
        assertThat(result.getThirdPartyAppAuthentication()).isNotNull();

        // We should have iOS and android payload even if we don't have an auto start token
        assertThat(result.getThirdPartyAppAuthentication().getIos()).isNotNull();
        assertThat(result.getThirdPartyAppAuthentication().getAndroid()).isNotNull();
    }
}
