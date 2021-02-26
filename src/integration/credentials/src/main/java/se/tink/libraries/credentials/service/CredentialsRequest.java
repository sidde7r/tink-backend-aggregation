package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.uuid.UUIDUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CredentialsRequest {
    // OperationId will be null when originating from CredentialsService...
    // Will be set when coming from new Authentication & Aggregation Engine
    private String operationId;
    private Credentials credentials;
    private Provider provider;
    private User user;
    private String userDeviceId;
    private List<Account> accounts;
    private String appUriId;
    private String callbackUri;

    // To be @Deprecated when UserPresence.originatingUserIp is confirmed present everywhere
    // @deprecated use the originatingUserIp inside UserPresence object instead
    private String originatingUserIp;

    private UserAvailability userAvailability;

    @JsonProperty("forceAuthenticate")
    private boolean forceAuthenticate;
    // TODO: Remove with new AgentWorker
    protected boolean create;
    // TODO: Remove with new AgentWorker
    protected boolean update;

    private List<DataFetchingRestrictions> dataFetchingRestrictions = new ArrayList<>();

    /** @return true if and only if this request was not initiated by a cron job */
    @JsonIgnore
    public abstract boolean isManual();

    @JsonIgnore
    public abstract CredentialsRequestType getType();

    public CredentialsRequest() {}

    public CredentialsRequest(User user, Provider provider, Credentials credentials) {
        this.user = user;
        this.provider = provider;
        this.credentials = credentials;
    }

    public CredentialsRequest(
            User user, Provider provider, Credentials credentials, String originatingUserIp) {
        this(user, provider, credentials);
        this.originatingUserIp = originatingUserIp;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public Provider getProvider() {
        return provider;
    }

    /** @return a user, or null if user not supplied. */
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

        ImmutableSortedMap<String, String> sortedFields =
                ImmutableSortedMap.copyOf(credentials.getFields());
        StringBuilder buffer = new StringBuilder();

        // Adding a Salt here to avoid dictionary attacks against Zookeeper locks.
        buffer.append(salt);

        for (Entry<String, String> entry : sortedFields.entrySet()) {
            // Important we are iterating over a sorted entryset here. Otherwise, field order could
            // be different
            // yielding different hash.

            buffer.append(entry.getKey());
            buffer.append("|");
            buffer.append(entry.getValue());
            buffer.append("||");
        }

        return String.format(
                "/locks/refreshCredentials/credentials/%s/%s",
                trimmedSHA1HexString(credentials.getProviderName()),
                trimmedSHA1HexString(buffer.toString()));
    }

    private static String trimmedSHA1HexString(String s) {
        // We don't need absolute full SHA1 checksums here. Risk of collision is minimal.
        // 16^10=1099511627776.
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

    public String getAppUriId() {
        return appUriId;
    }

    public void setAppUriId(String appUriId) {
        this.appUriId = appUriId;
    }

    public String getState() {
        // Use `appUriId` as state or, if not set, generate a random one.
        //
        // The strong authentication state carries information to main
        // regarding where to redirect the user.
        // That is why we must prefer using the appUriId (which is random)
        // as the state.
        // Last resort is to randomize it ourselves.
        if (appUriId != null) return appUriId;
        // Beware! Some financial institutes have limitations on
        // the state parameter. Known limitations:
        // - SDC only allow UUID.
        // - Barclays only allow ^(?!\s)(a-zA-Z0-9-_){1,255})$
        appUriId = UUIDUtils.generateUuidWithTinkTag();
        return appUriId;
    }

    public String getCallbackUri() {
        return callbackUri;
    }

    public void setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public List<DataFetchingRestrictions> getDataFetchingRestrictions() {
        return dataFetchingRestrictions;
    }

    public void setDataFetchingRestrictions(
            List<DataFetchingRestrictions> dataFetchingRestrictions) {
        this.dataFetchingRestrictions = dataFetchingRestrictions;
    }

    public String getOriginatingUserIp() {
        return originatingUserIp;
    }

    public void setOriginatingUserIp(String originatingUserIp) {
        this.originatingUserIp = originatingUserIp;
    }

    public boolean isForceAuthenticate() {
        return forceAuthenticate;
    }

    public void setForceAuthenticate(boolean forceAuthenticate) {
        this.forceAuthenticate = forceAuthenticate;
    }

    public UserAvailability getUserAvailability() {
        return userAvailability;
    }

    public void setUserAvailability(UserAvailability userAvailability) {
        this.userAvailability = userAvailability;
    }
}
