package se.tink.agent.runtime.instance;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

public class ProductionModuleFactory implements AgentModuleFactory {
    @Override
    public ImmutableSet<Module> getAgentModules() {
        return null;
    }
}
