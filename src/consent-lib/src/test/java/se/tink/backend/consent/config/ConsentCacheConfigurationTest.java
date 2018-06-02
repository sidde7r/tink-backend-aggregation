package se.tink.backend.consent.config;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsentCacheConfigurationTest {
    @Test
    public void testNoCacheWithSupplier() {
        constructAndVerifySupplier(ConsentCacheConfiguration.NoCache());
    }

    @Test
    public void testDefaultCacheWithSupplier() {
        constructAndVerifySupplier(ConsentCacheConfiguration.Default());
    }

    private void constructAndVerifySupplier(ConsentCacheConfiguration cache) {
        Supplier<String> supplier = Suppliers
                .memoizeWithExpiration(() -> "foo", cache.getDuration(), cache.getTimeUnit());

        assertThat(supplier.get()).isEqualTo("foo");
    }
}
