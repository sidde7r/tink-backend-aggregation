package se.tink.backend.system.workers.processor.chaining;

/**
 * Abstract factory that chooses which transaction chaining a user should use.
 */
public interface UserChainFactoryCreator {
    ChainFactory build();
}
