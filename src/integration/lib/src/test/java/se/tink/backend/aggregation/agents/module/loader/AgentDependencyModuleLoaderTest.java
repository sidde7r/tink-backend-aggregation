package se.tink.backend.aggregation.agents.module.loader;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.module.loader.testclasses.TestAgentLoadingInvalidModule;
import se.tink.backend.aggregation.agents.module.loader.testclasses.TestAgentLoadingNoModule;
import se.tink.backend.aggregation.agents.module.loader.testclasses.TestAgentLoadingTwoModules;
import se.tink.backend.aggregation.agents.module.loader.testclasses.TestAgentLoadingValidModule;
import se.tink.backend.aggregation.agents.module.loader.testclasses.ValidModule;
import se.tink.backend.aggregation.agents.module.loader.testclasses.ValidModule2;

public final class AgentDependencyModuleLoaderTest {

    private AgentDependencyModuleLoader loader;

    @Before
    public void setup() {
        loader = new AgentDependencyModuleLoader();
    }

    @Test
    public void ensureAgentWithSingleValidModuleLoadsCorrectly()
            throws ReflectiveOperationException {

        // given
        final Class<TestAgentLoadingValidModule> agentClass = TestAgentLoadingValidModule.class;
        final Set<Module> expectedResult = ImmutableSet.of(new ValidModule());

        // when
        Set<Module> loadedModules = loader.getModulesFromAnnotation(agentClass);

        // then
        assertThat(loadedModules, equalTo(expectedResult));
    }

    @Test
    public void ensureAgentWithMultipleValidModuleLoadsCorrectly()
            throws ReflectiveOperationException {

        // given
        final Class<TestAgentLoadingTwoModules> agentClass = TestAgentLoadingTwoModules.class;
        final Set<Module> expectedResult = ImmutableSet.of(new ValidModule(), new ValidModule2());

        // when
        Set<Module> loadedModules = loader.getModulesFromAnnotation(agentClass);

        // then
        assertThat(loadedModules, equalTo(expectedResult));
    }

    @Test(expected = NoSuchMethodException.class)
    public void ensureAgentWithInvalidModuleThrowsException() throws ReflectiveOperationException {

        // given
        final Class<TestAgentLoadingInvalidModule> agentClass = TestAgentLoadingInvalidModule.class;

        // when, then
        loader.getModulesFromAnnotation(agentClass);
    }

    @Test
    public void ensureAgentBindingNoModulesYieldsEmptySet() throws ReflectiveOperationException {

        // given
        final Class<TestAgentLoadingNoModule> agentClass = TestAgentLoadingNoModule.class;
        final Set<Module> expectedResult = ImmutableSet.of();

        // when
        Set<Module> loadedModules = loader.getModulesFromAnnotation(agentClass);

        // then
        assertThat(loadedModules, equalTo(expectedResult));
    }
}
