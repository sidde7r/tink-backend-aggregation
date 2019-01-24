package se.tink.backend.aggregation.agents.abnamro.ics;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import se.tink.backend.aggregation.agents.abnamro.utils.AbnAmroIcsCredentials;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.utils.mappers.CoreCredentialsMapper;
import se.tink.libraries.strings.StringUtils;

public class IcsAgentTest extends AbstractAgentTest<IcsAgent> {

    public IcsAgentTest() {
        super(IcsAgent.class);
    }

    @Test
    public void testUserWithOneCreditCard() throws Exception {

        // The credentials below changes after every data refresh at ABN so it is not sure that test works after a
        // refresh

        AbnAmroIcsCredentials credentials = AbnAmroIcsCredentials.create(StringUtils.generateUUID(), "64317013",
                        ImmutableSet.of(4818618001336577L));

        testAgent(CoreCredentialsMapper.toAggregationCredentials(credentials.getCredentials()));
    }
}
