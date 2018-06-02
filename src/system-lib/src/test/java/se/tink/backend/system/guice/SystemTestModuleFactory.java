package se.tink.backend.system.guice;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;

import java.util.List;

public class SystemTestModuleFactory {
    public static List<Module> getDefaultModules() {
        return ImmutableList.of(
                new TestRepositoriesModule(),
                new TestProcessorComponents()
        );
    }
}
