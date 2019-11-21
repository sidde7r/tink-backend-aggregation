package se.tink.sa.agent.facade.command;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public abstract class AbstractCommand<Q, S> implements StandaloneAgentCommand<Q, S> {

    private String target;

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public final S execute(Q request) {
        final ManagedChannel channel =
                ManagedChannelBuilder.forTarget(target).usePlaintext(true).build();
        S response = null;
        try {
            doExecute(channel, request);
        } finally {
            channel.shutdownNow();
        }
        ;
        return response;
    }

    protected abstract S doExecute(ManagedChannel channel, Q request);
}
