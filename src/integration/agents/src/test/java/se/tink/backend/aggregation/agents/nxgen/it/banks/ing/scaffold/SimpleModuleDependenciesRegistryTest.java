package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.RandomDataProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.ModuleDependenciesRegistry.BeanNotFoundException;

public class SimpleModuleDependenciesRegistryTest {

    private SimpleModuleDependenciesRegistry simpleModuleBeansRegistry =
            new SimpleModuleDependenciesRegistry();

    @Test
    public void registerBeanShouldAddBeanToRegistryAndThenGetBean() {
        // given
        RandomDataProvider givenRandomDataProvider = new RandomDataProvider();
        ConfigurationProvider givenConfigurationProvider = new ConfigurationProvider() {};

        // when
        simpleModuleBeansRegistry.registerBean(RandomDataProvider.class, givenRandomDataProvider);
        simpleModuleBeansRegistry.registerBean(
                ConfigurationProvider.class, givenConfigurationProvider);

        // then
        Assertions.assertThat(simpleModuleBeansRegistry.getBean(RandomDataProvider.class))
                .isEqualTo(givenRandomDataProvider);
        Assertions.assertThat(simpleModuleBeansRegistry.getBean(ConfigurationProvider.class))
                .isEqualTo(givenConfigurationProvider);
    }

    @Test(expected = BeanNotFoundException.class)
    public void getBeanShouldThrowBeanNotFoundExceptionWhenBeanNotRegistered() {
        // then
        simpleModuleBeansRegistry.getBean(ConfigurationProvider.class);
    }
}
