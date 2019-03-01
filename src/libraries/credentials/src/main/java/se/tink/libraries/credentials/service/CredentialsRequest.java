package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableSortedMap;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.user.rpc.User;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CredentialsRequest {
    private Credentials credentials;
    private Provider provider;
    private User user;
    private String userDeviceId;
    private List<Account> accounts;

    // TODO: Remove with new AgentWorker
    protected boolean create;
    // TODO: Remove with new AgentWorker
    protected boolean update;

    /**
     * @return true if and only if this request was not initiated by a cron job
     */
    @JsonIgnore
    public abstract boolean isManual();

    @JsonIgnore
    public abstract CredentialsRequestType getType();

    public CredentialsRequest() {

    }

    public CredentialsRequest(User user, Provider provider, Credentials credentials) {
        this.user = user;
        this.provider = provider;
        this.credentials = credentials;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public Provider getProvider() {
        return provider;
    }

    /**
     * @return a user, or null if user not supplied.
     */
    public User getUser() {
        return user;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserDeviceId() {
        return userDeviceId;
    }

    public void setUserDeviceId(String userDeviceId) {
        this.userDeviceId = userDeviceId;
    }

    public String constructLockPath(String salt) {
        Credentials credentials = getCredentials();

        ImmutableSortedMap<String, String> sortedFields = ImmutableSortedMap.copyOf(credentials.getFields());
        StringBuffer buffer = new StringBuffer();

        // Adding a Salt here to avoid dictionary attacks against Zookeeper locks.
        buffer.append(salt);

        for (Entry<String, String> entry : sortedFields.entrySet()) {
            // Important we are iterating over a sorted entryset here. Otherwise, field order could be different
            // yielding different hash.

            buffer.append(entry.getKey());
            buffer.append("|");
            buffer.append(entry.getValue());
            buffer.append("||");
        }

        return String.format("/locks/refreshCredentials/credentials/%s/%s",
                trimmedSHA1HexString(credentials.getProviderName()), trimmedSHA1HexString(buffer.toString()));
    }

    private static String trimmedSHA1HexString(String s) {
        // We don't need absolute full SHA1 checksums here. Risk of collision is minimal. 16^10=1099511627776.
        return sha1ToHexString(s).substring(0, 10);
    }

    private static String sha1ToHexString(String s) {
        return new String(Hex.encodeHex(StringUtils.hashSHA1(s)));
    }

    public List<Account> getAccounts() {
        return Objects.nonNull(accounts) ? accounts : Collections.emptyList();
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }
}
