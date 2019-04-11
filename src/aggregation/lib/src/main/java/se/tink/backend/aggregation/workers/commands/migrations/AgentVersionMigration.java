package se.tink.backend.aggregation.workers.commands.migrations;

import com.google.api.client.util.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class AgentVersionMigration {

    public static final String DUPLICATE = "-duplicate";
    private ControllerWrapper wrapper;

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
        List<Account> accountList =
                deduplicateAccounts(accounts).stream()
                        .map(a -> migrateAccount(a))
                        .collect(Collectors.toList());

        request.setAccounts(accountList);
    }

    /**
     * This method sends the update request to system endpoint
     *
     * @param account with new {@link Account#bankId} should be passed
     * @return
     */
    protected Account migrateAccount(Account account) {
        return getControlWrapper().updateAccountMetaData(account.getId(), account.getBankId());
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
     *     suffix -duplicate)
     */
    private List<Account> deduplicateAccounts(List<Account> list) {
        List<Account> deduplicatedList = new ArrayList<>();
        Map<String, List<Account>> duplicatesDetection = new HashMap<>();
        // Find duplicated accounts
        list.stream()
                .forEach(
                        a -> {
                            if (!duplicatesDetection.containsKey(a.getBankId())) {
                                duplicatesDetection.put(a.getBankId(), Lists.newArrayList());
                            }
                            duplicatesDetection.get(a.getBankId()).add(a);
                        });
        // Add all not duplicted accounts
        deduplicatedList.addAll(
                duplicatesDetection.entrySet().stream()
                        .filter(e -> e.getValue().size() == 1)
                        .map(e -> e.getValue().get(0))
                        .collect(Collectors.toList()));

        // Deduplicat Accounts by adding '-duplicate' at the end of bankid
        duplicatesDetection.entrySet().stream()
                .filter(e -> e.getValue().size() == 2)
                .map(e -> e.getValue())
                .forEach(
                        l -> {
                            // Look for opened accounts and mark one of them as duplicate
                            // if no open accounts - choose randomly
                            Account account =
                                    l.stream()
                                            .filter(a -> !a.isClosed())
                                            .findAny()
                                            .orElse(l.get(0));
                            account.setBankId(account.getBankId() + DUPLICATE);
                            deduplicatedList.addAll(l);
                        });

        return deduplicatedList;
    }
}
