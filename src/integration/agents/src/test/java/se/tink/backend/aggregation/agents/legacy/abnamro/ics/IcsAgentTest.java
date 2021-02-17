package se.tink.backend.aggregation.agents.abnamro.ics;

import com.google.common.collect.ImmutableSet;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.abnamro.utils.AbnAmroIcsCredentials;
import se.tink.backend.aggregation.agents.framework.legacy.AbstractAgentTest;
import se.tink.libraries.uuid.UUIDUtils;

public class IcsAgentTest extends AbstractAgentTest<IcsAgent> {

    public IcsAgentTest() {
        super(IcsAgent.class);
    }

    @Test
    @Ignore("Broken test")
    public void testUserWithOneCreditCard() throws Exception {

        // The credentials below changes after every data refresh at ABN so it is not sure that test
        // works after a
        // refresh

        AbnAmroIcsCredentials credentials =
                AbnAmroIcsCredentials.create(
                        UUIDUtils.generateUUID(), "64317013", ImmutableSet.of(4818618001336577L));

        testAgent(credentials.getCredentials());
    }
}
