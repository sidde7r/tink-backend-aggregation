package se.tink.backend.aggregation.workers.commands.migrations;

import com.google.api.client.util.Lists;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class AgentVersionMigration {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String DUPLICATE = "-duplicate-";
    private ControllerWrapper wrapper;
    private ClientInfo clientIfo;

    /**
     * Depending on result of this method the migrations will be performed or completely omitted. In
     * implementation the class agent given in {@param request} can be obtained by using {@link
     * Provider#getClassName() getClassName} method on the {@link CredentialsRequest#getProvider()
     * provider}. Then this value needs to be inspected. If the {@link String className} corresponds
     * to the implementation that uses new {@link Account#bankId} format there is no need of
     * performing a migrations, so <code>false</code> should ve returned. When the {@link String
     * className} is different, what usually means that it is pointing the previous version of the
     * agent implementation that uses an old {@link Account#bankId} format the migration is
     * necessary and <code>true</code> should be returned.
     *
     * @param request
     * @return <code>false</code> if the {@param request} was done by a new agent version, <code>
     *     true
     *     </code> if it uses previous version of agent
     */
    public abstract boolean shouldChangeRequest(CredentialsRequest request);

    /**
     * Performs a check if data associated with these credentials was already migrated.
     * This check is needed as we do not switch to new agent if all of the data was not migrated
     * @param request
     * @return <code>false</> if the {@link CredentialsRequest#getAccounts() account}'s {@link Account#bankId} format corresponds to the new one,
     * <code>true</code> if it's the old format
     */
    public abstract boolean shouldMigrateData(CredentialsRequest request);

    /**
     * Changing the agent that will be used in later processing by using the {@link
     * Provider#setClassName(String) set} method on {@link CredentialsRequest#getProvider() provider
     * in the {@param request}}
     *
     * @param request
     */
    public abstract void changeRequest(CredentialsRequest request);

    /**
     * Implementation of actual migration of {@link Account#bankId} format to the new one. Accounts
     * after the change should be set on the {@link CredentialsRequest request} by the {@link
     * CredentialsRequest#setAccounts(List) setAccounts} method
     *
     * @param request
     */
    public abstract void migrateData(CredentialsRequest request);

    /**
     * Wrapper method that makes sure {@link
     * AgentVersionMigration#migrateAccounts(CredentialsRequest, List)} method is executed and sync
     * with the {@link Account#bankId} value from the {@param request}
     *
     * @param request
     */
    public void updateAccounts(CredentialsRequest request) {
        migrateData(request);
        migrateAccounts(request, request.getAccounts());
    }

    /**
     * This method update the {@param request} with new {@param accounts} and executes the {@link
     * AgentVersionMigration#migrateAccount(Account)} for each account
     *
     * @param request
     * @param accounts
     */
    protected void migrateAccounts(CredentialsRequest request, List<Account> accounts) {

        // Preventively generate the description of the accounts, before the objects get modified
        String originalAccountsDescription =
                Arrays.toString(accounts.stream().map(Account::getBankId).toArray());

        List<Account> accountList = deduplicateAccounts(accounts);
        try {

            accountList =
                    accountList.stream().map(this::migrateAccount).collect(Collectors.toList());
        } catch (UniformInterfaceException e) {

            logger.error(
                    String.format(
                            "Error updating migrated accounts. Pre: %s Post: %s",
                            originalAccountsDescription,
                            Arrays.toString(
                                    accountList.stream().map(Account::getBankId).toArray())));
            throw e;
        }

        request.setAccounts(accountList);
    }

    /**
     * This method sends the update request to system endpoint
     *
     * @param account with new {@link Account#bankId} should be passed
     * @return
     */
    protected Account migrateAccount(Account account) {
        try {
            return getControlWrapper().updateAccountMetaData(account.getId(), account.getBankId());
        } catch (UniformInterfaceException e) {
            logger.error(
                    String.format(
                            "Error migrating account %s to %s",
                            account.getId(), account.getBankId()));
            throw e;
        }
    }

    public void setClientIfo(ClientInfo clientInfo) {
        this.clientIfo = clientInfo;
    }

    protected ClientInfo getClientIfo() {
        return clientIfo;
    }

    public final void setWrapper(ControllerWrapper wrapper) {
        this.wrapper = wrapper;
    }

    private final ControllerWrapper getControlWrapper() {
        return this.wrapper;
    }

    /**
     * * This method tag duplicated accounts with adding `-duplicate` at the end of {@link *
     * Account#bankId}. To decide which account should be market as duplicate the {@link *
     * Account#closed} property is used. When this property is set to <code>true</code> we consider
     * * this account as older and one which might have longer history, so we would like to continue
     * * with using this account after migration.
     *
     * @param list that should be deduplicated
     * @return {@link List<Account>} that contains all the accounts with different bankId (add
     *     suffix -duplicate-{n} where n is incremented for each duplicate, starting at 1)
     */
    private List<Account> deduplicateAccounts(List<Account> list) {
        Map<String, List<Account>> duplicatesDetection = new HashMap<>();
        // Find duplicated accounts
        list.forEach(
                a -> {
                    if (!duplicatesDetection.containsKey(a.getBankId())) {
                        duplicatesDetection.put(a.getBankId(), Lists.newArrayList());
                    }
                    duplicatesDetection.get(a.getBankId()).add(a);
                });
        // Add all not duplicated accounts
        List<Account> deduplicatedList =
                duplicatesDetection.entrySet().stream()
                        .filter(e -> e.getValue().size() == 1)
                        .map(e -> e.getValue().get(0))
                        .collect(Collectors.toList());

        // Deduplicate Accounts by adding '-duplicate-{n}' at the end of bankid
        duplicatesDetection.entrySet().stream()
                .filter(e -> e.getValue().size() >= 2)
                .map(Entry::getValue)
                .forEach(
                        duplicates -> {

                            // Sort closed accounts first, assuming that these are older.
                            duplicates.sort(
                                    Comparator.comparing(
                                            Account::isClosed, Comparator.reverseOrder()));

                            // Tag all but the first account in the list with '-duplicate-{n}'.
                            for (int i = 1; i < duplicates.size(); i++) {

                                logger.warn("Tagging duplicate account with -duplicate-" + i);
                                Account duplicate = duplicates.get(i);
                                duplicate.setBankId(duplicate.getBankId() + DUPLICATE + i);
                            }

                            deduplicatedList.addAll(duplicates);
                        });

        return deduplicatedList;
    }
}
