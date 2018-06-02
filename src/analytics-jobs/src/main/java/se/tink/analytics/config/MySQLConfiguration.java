package se.tink.analytics.config;

public class MySQLConfiguration {

    public final MySQLCredentials credentials;
    public final String database;
    public final String hostname;

    public MySQLConfiguration(String hostname, String database, MySQLCredentials credentials) {
        this.hostname = hostname;
        this.database = database;
        this.credentials = credentials;
    }

}
