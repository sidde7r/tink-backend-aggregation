package se.tink.backend.aggregation.agents.module.loader;

import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.module.loader.correctnestedmodules.TopLevelModule;
import se.tink.backend.aggregation.agents.module.loader.correctnestedmodules.subpackage.SubPackageModule;

public final class AgentPackageModuleLoaderTest {

    private PackageModuleLoader loader;
    private String thisPackage;

    @Before
    public void setup() {
        loader = new PackageModuleLoader();
        thisPackage = this.getClass().getPackage().getName();
    }

    @Test
    public void ensureAllModulesInNestedPackageStructureAreLoadedAndTestModuleIsIgnored()
            throws ReflectiveOperationException {

        final String correctNestedModules = "correctnestedmodules";
        final Set<Module> expectedResult =
                ImmutableSet.of(new TopLevelModule(), new SubPackageModule());

        Set<Module> loadedModules =
                loader.getModulesInPackage(thisPackage + "." + correctNestedModules);

        Assert.assertThat(loadedModules, equalTo(expectedResult));
    }

    @Test(expected = NoSuchMethodException.class)
    public void ensureModuleThatCantBeInstantiatedThrowsException()
            throws ReflectiveOperationException {

        final String incorrectModule = "incorrectmodule";

        loader.getModulesInPackage(thisPackage + "." + incorrectModule);
    }
}
