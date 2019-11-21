package se.tink.sa.framework.command;

public interface AgentCommand<Q, S> {

    S execute(Q request);
}
