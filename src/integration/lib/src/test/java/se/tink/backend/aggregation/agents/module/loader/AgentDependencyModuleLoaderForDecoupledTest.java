package se.tink.backend.aggregation.agents.module.loader;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.module.loader.testclasses.TestAgentLoadingModulesForDifferentModes;
import se.tink.backend.aggregation.agents.module.loader.testclasses.ValidModule;
import se.tink.backend.aggregation.agents.module.loader.testclasses.ValidModule2;

public class AgentDependencyModuleLoaderForDecoupledTest {
    private AgentDependencyModuleLoaderForDecoupled loader;

    @Before
    public void setup() {
        loader = new AgentDependencyModuleLoaderForDecoupled();
    }

    @Test
    public void ensureAgentLoadsCorrectly() throws NoSuchMethodException {
        // given
        final Class<TestAgentLoadingModulesForDifferentModes> agentClass =
                TestAgentLoadingModulesForDifferentModes.class;
        final Set<Module> expectedResult = ImmutableSet.of(new ValidModule(), new ValidModule2());

        // when
        Set<Module> loadedModules = loader.getModulesFromAnnotation(agentClass);

        // then
        assertThat(loadedModules, equalTo(expectedResult));
    }
}
