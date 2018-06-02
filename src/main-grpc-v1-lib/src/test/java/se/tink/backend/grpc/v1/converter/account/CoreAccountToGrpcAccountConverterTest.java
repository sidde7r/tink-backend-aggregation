package se.tink.backend.grpc.v1.converter.account;

import com.google.common.collect.Maps;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import static org.assertj.core.api.Assertions.assertThat;

public class CoreAccountToGrpcAccountConverterTest {
    @Test
    public void testConvertAccountWithoutIdentifiers() {
        CoreAccountToGrpcAccountConverter converter = new CoreAccountToGrpcAccountConverter("SEK", Maps.newHashMap());

        Account account = new Account();

        se.tink.grpc.v1.models.Account result = converter.convertFrom(account);

        assertThat(result.getIdentifiersList().isEmpty()).isTrue();
    }

    @Test
    public void testConvertAccountWithOneIdentifier() {
        CoreAccountToGrpcAccountConverter converter = new CoreAccountToGrpcAccountConverter("SEK", Maps.newHashMap());

        AccountIdentifier identifier = AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233");

        Account account = new Account();
        account.putIdentifier(identifier);

        se.tink.grpc.v1.models.Account result = converter.convertFrom(account);

        assertThat(result.getIdentifiersList().isEmpty()).isFalse();
        assertThat(result.getIdentifiersList().get(0)).isEqualTo(identifier.toURI().toString());
    }

    @Test
    public void testConvertAccountWithMultipleIdentifiers() {
        CoreAccountToGrpcAccountConverter converter = new CoreAccountToGrpcAccountConverter("SEK", Maps.newHashMap());

        AccountIdentifier identifier1 = AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233");
        AccountIdentifier identifier2 = AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112234");

        Account account = new Account();
        account.putIdentifier(identifier1);
        account.putIdentifier(identifier2);

        se.tink.grpc.v1.models.Account result = converter.convertFrom(account);

        assertThat(result.getIdentifiersList().isEmpty()).isFalse();
        assertThat(result.getIdentifiersList()).containsOnly(identifier1.toURI().toString(),
                identifier2.toURI().toString());
    }

}
