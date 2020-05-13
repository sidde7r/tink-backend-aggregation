package se.tink.backend.aggregation.agents.module.loader;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.module.loader.correctnestedmodules.module.AgentModule;

public final class AgentPackageModuleLoaderTest {

    private PackageModuleLoader loader;
    private String thisPackage;

    @Before
    public void setup() {
        loader = new PackageModuleLoader();
        thisPackage = this.getClass().getPackage().getName();
    }

    @Test
    public void ensureTheModuleInProperFolderWithProperNameIsLoaded()
            throws ReflectiveOperationException {

        // given
        final String correctNestedModules = "correctnestedmodules";
        final Set<Module> expectedResult = ImmutableSet.of(new AgentModule());

        // when
        Set<Module> loadedModules =
                loader.getModulesInPackage(thisPackage + "." + correctNestedModules);

        // then
        assertThat(loadedModules, equalTo(expectedResult));
    }

    @Test
    public void ensureNoModuleIsLoadedWhenModuleIsNotInProperFolder()
            throws ReflectiveOperationException {

        // given
        final String incorrectModule = "incorrectmodule";

        // when
        Set<Module> loadedModules = loader.getModulesInPackage(thisPackage + "." + incorrectModule);

        // then
        Assert.assertTrue(loadedModules.isEmpty());
    }
}
