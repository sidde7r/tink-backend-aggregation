package se.tink.backend.aggregation.agents.banks.se;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.banks.se.collector.CollectorAgent;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.backend.aggregation.rpc.ProductType;
import se.tink.backend.common.utils.TestSSN;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;

public class CollectorAgentTest extends AbstractAgentTest<CollectorAgent> {
    private Credentials credentials = new Credentials();
    private SwedishIdentifier withdrawalAccount = new SwedishIdentifier("1234, 123456789-0");

    private static final String SSN = "201212121212";
    private static final String STREET_ADDRESS = "Street 1A";
    private static final String ZIP_CODE = "12345";
    private static final String CITY = "stockholm";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";

    public CollectorAgentTest() {
        super(CollectorAgent.class);
    }

    private static Credentials credentials(String pnr) {
        Credentials c = new Credentials();
        c.setUsername(pnr);
        c.setType(CredentialsTypes.MOBILE_BANKID);
        return c;
    }
    @Before
    public void setup() {
        credentials = credentials(SSN);
    }

    @Test
    public void testLoginWithBankID() throws Exception {
        testAgent(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testPersistentLogin() throws Exception {
        testAgentPersistentLoggedIn(credentials);
    }

}
