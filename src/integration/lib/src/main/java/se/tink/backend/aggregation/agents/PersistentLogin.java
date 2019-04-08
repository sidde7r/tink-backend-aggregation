package se.tink.backend.aggregation.agents;

public interface PersistentLogin {
    /** Return true or false whenever the agent is logged in against the bank or service */
    boolean isLoggedIn() throws Exception;

    /** Keep the session alive against the bank or service */
    boolean keepAlive() throws Exception;

    /** Persist the login session so it can be re-used when the agent is recreated */
    void persistLoginSession();

    /** Load any existing sessions */
    void loadLoginSession();

    /** Clear any existing sessions */
    void clearLoginSession();
}
