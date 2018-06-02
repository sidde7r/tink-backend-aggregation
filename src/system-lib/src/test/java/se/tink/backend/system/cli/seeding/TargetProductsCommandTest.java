package se.tink.backend.system.cli.seeding;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.common.product.targeting.Profile;
import se.tink.backend.common.product.targeting.ProviderCapabilityPredicate;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.product.ProductFilterRule;
import se.tink.backend.core.product.ProductFilterRuleType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TargetProductsCommandTest {
    @Test
    public void providerCapabilityPredicate_whenHavingTransferCapableProvider() {
        ProductFilterRule rule = createTransferCapabilityFilter();

        ImmutableMap<String, Provider> providerMap = createProviderMap(ImmutableSet.of(Provider.Capability.TRANSFERS));
        CredentialsRepository mockedCredentialsRepository = mockCredentialsRepository();
        
        User user = new User();

        ImmutableListMultimap<String, Credentials> credentialsByProviderName = FluentIterable
                .from(mockedCredentialsRepository.findAllByUserId(user.getId()))
                .index(Credentials::getProviderName);

        assertThat(new ProviderCapabilityPredicate(rule, providerMap)
                .apply(new Profile(user, credentialsByProviderName, null)))
                .isTrue();
    }

    @Test
    public void providerCapabilityPredicate_whenNoTransferCapableProvider() {
        ProductFilterRule rule = createTransferCapabilityFilter();

        ImmutableMap<String, Provider> providerMap = createProviderMap(ImmutableSet.<Provider.Capability>of());
        CredentialsRepository mockedCredentialsRepository = mockCredentialsRepository();

        User user = new User();

        ImmutableListMultimap<String, Credentials> credentialsByProviderName = FluentIterable
                .from(mockedCredentialsRepository.findAllByUserId(user.getId()))
                .index(Credentials::getProviderName);

        assertThat(new ProviderCapabilityPredicate(rule, providerMap)
                .apply(new Profile(user, credentialsByProviderName, null)))
                .isFalse();
    }

    private ProductFilterRule createTransferCapabilityFilter() {
        return new ProductFilterRule(
                    ProductFilterRuleType.PROVIDER_CAPABILITY,
                    Provider.Capability.TRANSFERS.name());
    }

    private static CredentialsRepository mockCredentialsRepository() {
        CredentialsRepository mockedCredentialsRepository = mock(CredentialsRepository.class);

        Credentials credential = new Credentials();
        credential.setProviderName("provider-name");

        when(mockedCredentialsRepository.findAllByUserId(anyString()))
                .thenReturn(Collections.singletonList(credential));

        return mockedCredentialsRepository;
    }

    private static ImmutableMap<String, Provider> createProviderMap(ImmutableSet<Provider.Capability> capabilities) {
        Provider provider = new Provider();
        provider.setCapabilities(capabilities);
        provider.setName("provider-name");

        return ImmutableMap.of(provider.getName(), provider);
    }
}
