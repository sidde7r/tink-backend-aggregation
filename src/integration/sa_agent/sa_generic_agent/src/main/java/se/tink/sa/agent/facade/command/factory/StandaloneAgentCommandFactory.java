package se.tink.sa.agent.facade.command.factory;

import se.tink.sa.agent.facade.command.SaAgentLoginCommand;

public class StandaloneAgentCommandFactory {

    // TODO: mock
    private String target = "localhost:8085";

    private static StandaloneAgentCommandFactory INSTANCE;

    private StandaloneAgentCommandFactory() {}

    public static synchronized StandaloneAgentCommandFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StandaloneAgentCommandFactory();
        }
        return INSTANCE;
    }

    public SaAgentLoginCommand buildSaAgentLoginCommand() {
        SaAgentLoginCommand command = new SaAgentLoginCommand();
        command.setTarget(target);
        return command;
    }
}
