package se.tink.backend.aggregation.nxgen.core.account;

import java.util.List;

import org.junit.Ignore;
import org.mockito.Mockito;
import se.tink.libraries.account.AccountIdentifier;

@Ignore
public class TestAccountBuilder<T extends Account> {

    private T account;

    private TestAccountBuilder(Class<T> cls) {
        this.account = Mockito.mock(cls);
        Mockito.when(account.getType()).thenCallRealMethod();
    }

    public static <T extends Account> TestAccountBuilder<T> from(Class<T> accountType) {
        return new TestAccountBuilder<>(accountType);
    }

    public TestAccountBuilder setName(String name) {
        Mockito.when(account.getName()).thenReturn(name);
        return this;
    }

    public TestAccountBuilder setAccountNumber(String accountNumber) {
        Mockito.when(account.getAccountNumber()).thenReturn(accountNumber);
        return this;
    }

    public TestAccountBuilder setIdentifiers(List<AccountIdentifier> identifiers) {
        Mockito.when(account.getIdentifiers()).thenReturn(identifiers);
        return this;
    }

    public T build() {
        return account;
    }
}
