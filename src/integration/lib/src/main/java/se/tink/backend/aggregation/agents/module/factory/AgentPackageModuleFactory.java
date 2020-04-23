package se.tink.backend.aggregation.agents.module.factory;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Module;
import java.util.Set;
import se.tink.backend.aggregation.agents.agentfactory.AgentModuleFactory;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.module.AgentComponentProviderModule;
import se.tink.backend.aggregation.agents.module.AgentRequestScopeModule;
import se.tink.backend.aggregation.agents.module.loader.PackageModuleLoader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AgentPackageModuleFactory implements AgentModuleFactory {

    private static final String DEFAULT_AGENT_PACKAGE_CLASS_PREFIX =
            "se.tink.backend.aggregation.agents";

    private final PackageModuleLoader packageModuleLoader;

    @Inject
    public AgentPackageModuleFactory(PackageModuleLoader packageModuleLoader) {
        this.packageModuleLoader = packageModuleLoader;
    }

    /**
     * Gets modules needed to run the agent in production. It will load a set of default
     * dependencies as well as any modules specified under the agent package.
     *
     * @param request CredentialsRequest to bind.
     * @param context Context to bind.
     * @param configuration Configuration to bind.
     * @return Set of modules that specifies bindings for the agent instantiation.
     * @throws ReflectiveOperationException If something went wrong when looking for modules in
     *     agent package.
     */
    @Override
    public ImmutableSet<Module> getAgentModules(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration)
            throws ReflectiveOperationException {

        return ImmutableSet.<Module>builder()
                .add(new AgentComponentProviderModule())
                .add(new AgentRequestScopeModule(request, context, configuration))
                .addAll(getAgentPackageModules(request.getProvider().getClassName()))
                .build();
    }

    /**
     * Looks for Guice modules in the agent package, getting all classes implementing {@link Module}
     * in this package.
     *
     * @param className Path to agent to get modules for.
     * @return Set of all concrete implementations of {@code Module} in given agent package.
     * @throws ReflectiveOperationException if one or more modules could not be instantiated.
     */
    private Set<Module> getAgentPackageModules(String className)
            throws ReflectiveOperationException {
        final int i = className.lastIndexOf('.');
        final String agentPackagePath = className.substring(0, i);

        return packageModuleLoader.getModulesInPackage(
                DEFAULT_AGENT_PACKAGE_CLASS_PREFIX + "." + agentPackagePath);
    }
}
