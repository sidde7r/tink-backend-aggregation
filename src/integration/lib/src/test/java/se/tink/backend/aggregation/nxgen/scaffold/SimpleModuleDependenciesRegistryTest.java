package se.tink.backend.aggregation.nxgen.scaffold;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.scaffold.ModuleDependenciesRegistry.BeanNotFoundException;

public class SimpleModuleDependenciesRegistryTest {

    private SimpleModuleDependenciesRegistry simpleModuleBeansRegistry =
            new SimpleModuleDependenciesRegistry();

    @Test
    public void registerBeanShouldAddBeanToRegistryAndThenGetBean() {
        // given
        TestBean givenTestBean = new TestBean();
        TestBean2 givenTestBean2 = new TestBean2() {};

        // when
        simpleModuleBeansRegistry.registerBean(TestBean.class, givenTestBean);
        simpleModuleBeansRegistry.registerBean(TestBean2.class, givenTestBean2);

        // then
        assertThat(simpleModuleBeansRegistry.getBean(TestBean.class)).isEqualTo(givenTestBean);
        assertThat(simpleModuleBeansRegistry.getBean(TestBean2.class)).isEqualTo(givenTestBean2);
    }

    @Test
    public void getBeanShouldThrowBeanNotFoundExceptionWhenBeanNotRegistered() {
        // when
        Throwable throwable =
                catchThrowable(() -> simpleModuleBeansRegistry.getBean(TestBean2.class));

        // then
        assertThat(throwable)
                .isInstanceOf(BeanNotFoundException.class)
                .hasMessage("Bean 'TestBean2' is not registered");
    }

    private static class TestBean {}

    private static class TestBean2 {}
}
