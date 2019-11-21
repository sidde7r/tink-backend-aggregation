package se.tink.sa.framework.command;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractCommand<Q, S> implements AgentCommand<Q, S> {

    @Override
    public final S execute(Q request) {
        S response = null;
        try {
            response = doExecute(request);
        } catch (Exception ex) {
            log.error("Exception during processing request", ex);
        }

        return response;
    }

    protected abstract S doExecute(Q request);
}
