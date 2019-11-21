package se.tink.sa.agent.facade.command;

public interface StandaloneAgentCommand<Q, S> {

    S execute(Q request);
}
