package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.AgentFactoryImpl;
import se.tink.backend.aggregation.agents.agentfactory.AgentModuleFactory;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.provider.AgentProvider;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;
import se.tink.backend.aggregation.agents.framework.wiremock.module.AgentWireMockModuleFactory;
import se.tink.backend.aggregation.agents.module.loader.AgentDependencyModuleLoader;
import se.tink.backend.aggregation.agents.module.loader.AgentDependencyModuleLoaderForDecoupled;

public final class AgentFactoryWireMockModule extends AbstractModule {

    private final FakeBankSocket fakeBankSocket;
    private final Map<String, String> callbackData;
    private final Module agentModule;
    private final List<Class<? extends CompositeAgentTestCommand>> commandSequence;

    public AgentFactoryWireMockModule(
            FakeBankSocket fakeBankSocket,
            Map<String, String> callbackData,
            Module agentModule,
            List<Class<? extends CompositeAgentTestCommand>> commandSequence) {
        this.fakeBankSocket = fakeBankSocket;
        this.callbackData = callbackData;
        this.agentModule = agentModule;
        this.commandSequence = commandSequence;
    }

    @Override
    protected void configure() {

        // TODO: Replace WireMockConfiguration, currently needed for AgentWireMockModuleFactory
        bind(FakeBankSocket.class).toInstance(fakeBankSocket);
        bind(WireMockConfiguration.class)
                .toInstance(
                        WireMockConfiguration.builder()
                                .setCallbackData(callbackData)
                                .setAgentModule(agentModule)
                                .build());
        bind(AgentModuleFactory.class).to(AgentWireMockModuleFactory.class).in(Scopes.SINGLETON);
        bind(AgentFactory.class).to(AgentFactoryImpl.class).in(Scopes.SINGLETON);
        bind(Agent.class).toProvider(AgentProvider.class).in(Scopes.SINGLETON);
        bind(AgentDependencyModuleLoader.class)
                .to(AgentDependencyModuleLoaderForDecoupled.class)
                .in(Scopes.SINGLETON);
        bindCommandsInSequence();
    }

    private void bindCommandsInSequence() {
        Multibinder<CompositeAgentTestCommand> commandBinder =
                Multibinder.newSetBinder(binder(), CompositeAgentTestCommand.class);
        commandSequence.forEach(commandClass -> bindCommand(commandBinder, commandClass));
    }

    private void bindCommand(
            Multibinder<CompositeAgentTestCommand> commandBinder,
            Class<? extends CompositeAgentTestCommand> clazz) {
        commandBinder.addBinding().to(clazz).in(Scopes.SINGLETON);
    }
}
