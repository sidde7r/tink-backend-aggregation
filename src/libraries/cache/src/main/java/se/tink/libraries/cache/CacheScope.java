package se.tink.libraries.cache;

/** Enumeration of cache namespaces. */
public enum CacheScope {
    SUGGEST_TRANSACTIONS_RESPONSE_BY_USERID("suggest-by-userId:"),
    FULL_REFRESH_TIMER_BY_CREDENTIALS("/timer/refreshCredentials/credentials/"),
    STATISTICS_BY_USERID("statistics-by-userId:"),
    CASSANDRA_STATISTICS_BY_USERID("cassandra-statistics-by-userId:"),
    SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID("supplementCredentials:"),
    ENCRYPTED_CREDENTIALS_BY_CREDENTIALSID("encryptedCredentials:"),
    FRAUD_DETAILS_BY_USERID("fraud-details-content-by-userId:"),
    CREDENTIALS_KEEP_ALIVE_BY_CREDENTIALSID("keepAliveCredentials:"),
    ACTIVITIES_TIMESTAMP_BY_USERID("/userState/activitiesTimestamp/"),
    CONTEXT_TIMESTAMP_BY_USERID("/userState/contextTimestamp/"),
    STATISTICS_TIMESTAMP_BY_USERID("/userState/statisticsTimestamp/"),
    ACTIVITY_BY_USERID_AND_ACTIVITYID("activities-by-userIdAndActivityId:"),
    ACTIVITIES_BY_USERID("activities-by-userId:"),
    SESSION_BY_ID("/sessions/"),
    FACEBOOK_ACCESS_TOKEN_BY_MD5("/access-tokens/fb/"),
    TRANSFER_BY_HASH("/transfers/hash/"),
    APPLICATION_LIMITER("/application-limiter/");

    private final String key;

    CacheScope(String key) {
        this.key = key;
    }

    public String withKey(String key) {
        return this.key + key;
    }
}
