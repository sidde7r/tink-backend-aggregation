package se.tink.backend.utils;

import java.util.Optional;
import org.junit.Test;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import static org.assertj.core.api.Assertions.assertThat;

public class ClearingNumberBankToProviderMapImplTest {
    @Test
    public void testShouldFindExistingBank() {
        ClearingNumberBankToProviderMapImpl clearingNumberBankToProviderMap = new ClearingNumberBankToProviderMapImpl();
        Optional<String> provider = clearingNumberBankToProviderMap.getProviderForBank(ClearingNumber.Bank.SEB);

        assertThat(provider.isPresent()).isTrue();
        assertThat(provider.get()).isEqualTo("seb");
    }

    @Test
    public void testShouldResolveMoreThanOneBank() {
        ClearingNumberBankToProviderMapImpl clearingNumberBankToProviderMap = new ClearingNumberBankToProviderMapImpl();
        Optional<String> provider = clearingNumberBankToProviderMap.getProviderForBank(ClearingNumber.Bank.SBAB);

        assertThat(provider.isPresent()).isTrue();
        assertThat(provider.get()).isEqualTo("sbab");
    }

    @Test
    public void testShouldReturnAbsentIfNotMapped() {
        ClearingNumberBankToProviderMapImpl clearingNumberBankToProviderMap = new ClearingNumberBankToProviderMapImpl();
        Optional<String> provider = clearingNumberBankToProviderMap.getProviderForBank(ClearingNumber.Bank.RIKSGALDEN);

        assertThat(provider.isPresent()).isFalse();
    }
}
