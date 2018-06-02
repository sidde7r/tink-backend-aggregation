package se.tink.backend.grpc.v1.converter.authentication.keys;

import org.junit.Test;
import se.tink.backend.rpc.auth.keys.StoreAuthenticationKeyCommand;
import se.tink.grpc.v1.models.AuthenticationKeyType;
import se.tink.grpc.v1.models.AuthenticationSource;
import se.tink.grpc.v1.rpc.StoreAuthenticationKeyRequest;
import static org.assertj.core.api.Assertions.assertThat;

public class StoreAuthenticationKeyRequestConverterTest {
    private static final String AUTHENTICATION_TOKEN = "SECRET TOKEN";
    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n"
            + "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQB+BROz0DQMG17liTc5yhNvgh1sJdI\n"
            + "MdZi0/1k8ax03ciZumBU0BDqM0/1MAUyrekFdXG+LZ0pM87rhYNrcRLu2WQBJrS6\n"
            + "ELr/XdJM+fty+7yvNmZ4sp6+dXPIwi+454zvujYuxxaMj6HyGforJUuszgOAEPOP\n"
            + "l1Aa/3c2ncA7lZNxkFk=\n"
            + "-----END PUBLIC KEY-----\n";
    private static StoreAuthenticationKeyRequestConverter converter = new StoreAuthenticationKeyRequestConverter();

    @Test
    public void convertAuthenticationKeyRequest_successful() {
        StoreAuthenticationKeyRequest request = StoreAuthenticationKeyRequest.newBuilder()
                .setAuthenticationToken(AUTHENTICATION_TOKEN)
                .setKey(PUBLIC_KEY)
                .setKeyType(AuthenticationKeyType.KEY_TYPE_ECDSA)
                .setSource(AuthenticationSource.AUTHENTICATION_SOURCE_TOUCHID).build();

        StoreAuthenticationKeyCommand command = converter.convertFrom(request);

        assertThat(command.getAuthenticationToken()).isEqualTo(AUTHENTICATION_TOKEN);
        assertThat(command.getKey()).isEqualTo(PUBLIC_KEY);
        assertThat(command.getSource()).isEqualTo(se.tink.backend.core.auth.AuthenticationSource.TOUCHID);
    }

    @Test
    public void convertAuthenticationKeyRequest_default() {
        StoreAuthenticationKeyRequest request = StoreAuthenticationKeyRequest.getDefaultInstance();
        StoreAuthenticationKeyCommand command = converter.convertFrom(request);

        assertThat(command.getAuthenticationToken()).isNullOrEmpty();
        assertThat(command.getKey()).isNullOrEmpty();
        assertThat(command.getSource()).isNull();
    }
}
