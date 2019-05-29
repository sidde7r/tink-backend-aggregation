package se.tink.backend.aggregation.workers.commands.migrations;

import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * Cluster safe version of {@link AgentVersionMigration}. This class should <strong>always</strong>
 * be used in favor of the non-cluster safe {@link AgentVersionMigration} unless you
 * <strong>explicitly need</strong> to control {@link #shouldChangeRequest(CredentialsRequest)},
 * {@link #changeRequest(CredentialsRequest)} or {@link #shouldMigrateData(CredentialsRequest)}.
 * This class ensures data safety in a multi-instance environment by doing the following that {@link
 * AgentVersionMigration} does not:
 *
 * <ul>
 *   <li>Triggering migration only on instances where the provider configuration has been updated to
 *       use the new agent class
 *   <li>On instances which do not yet have the new provider configuration, the new agent will be
 *       used only if the credential has already been migrated
 * </ul>
 *
 * <p>This behavior ensures that no race condition can ever occur where an old agent (in an instance
 * which does not yet have the latest configuration) is passed a migrated credential, and thus
 * creates a duplicate account.
 *
 * <p>This class handles the logic of ensuring the above. Implementing subclasses need only
 * implement the methods for checking agent status, migration status and doing the actual data
 * migration.
 */
public abstract class ClusterSafeAgentVersionMigration extends AgentVersionMigration {

    public abstract boolean isOldAgent(Provider provider);

    public abstract boolean isNewAgent(Provider provider);

    public abstract String getNewAgentClassName(Provider oldProvider);

    /**
     * Checks if the credentials request is compatible with the new agent.
     *
     * @param request
     * @return true if the data can be used by the new agent, false otherwise.
     */
    public abstract boolean isDataMigrated(CredentialsRequest request);

    @Override
    public final boolean shouldChangeRequest(CredentialsRequest request) {
        // Called by the worker command to decide if anything in the migration should run.
        // If using the old agent, the request will be changed to the new agent if the data is
        // already migrated.
        // If using the new agent, the request will be changed by updating the request data to the
        // new agent's format.
        final Provider provider = request.getProvider();
        return isOldAgent(provider) || isNewAgent(provider);
    }

    @Override
    public final void changeRequest(CredentialsRequest request) {
        // We only force switch already migrated credentials to the new agent, since this means that
        // we are in an instance with an old provider configuration.
        // Otherwise the provider configuration will make this switch for us.
        if (isDataMigrated(request)) {
            Provider provider = request.getProvider();
            provider.setClassName(getNewAgentClassName(provider));
        }
    }

    @Override
    public final boolean shouldMigrateData(CredentialsRequest request) {
        // We only migrate the data if we are in an instance that has the new agent activated and
        // about to receive an old credential.
        return isNewAgent(request.getProvider()) && !isDataMigrated(request);
    }
}
