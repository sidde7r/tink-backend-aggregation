package se.tink.agent.runtime.instance;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

public interface AgentModuleFactory {
    ImmutableSet<Module> getAgentModules();
}
