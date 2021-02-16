package se.tink.backend.aggregation.agents.creditcards.americanexpress;

import org.junit.Ignore;

/** Common stuff used by multiple American Express tests. */
@Ignore
public class CommonAmericanExpress {

    static final AmericanExpressCredential USER1 =
            new AmericanExpressCredential("fhedberg82", "gablanko24");
    static final AmericanExpressCredential USER2 =
            new AmericanExpressCredential("lunkan555", "Rebuqu83");

    /** American Express credentials used in testing. */
    protected static class AmericanExpressCredential {
        private String username;
        private String password;

        public AmericanExpressCredential(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getPassword() {
            return password;
        }

        public String getUsername() {
            return username;
        }
    }
}
