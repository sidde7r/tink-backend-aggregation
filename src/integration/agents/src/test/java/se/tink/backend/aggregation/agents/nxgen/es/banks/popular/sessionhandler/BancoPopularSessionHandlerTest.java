package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.sessionhandler;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularTestBase;

public class BancoPopularSessionHandlerTest extends BancoPopularTestBase {

    private BancoPopularSessionHandler sessionHandler;

    @Before
    public void setUp() throws Exception {
        super.setup();
        sessionHandler = new BancoPopularSessionHandler(bankClient);
    }

    @Test
    public void keepAlive() throws Exception {
        authenticate();

        sessionHandler.keepAlive();
    }
}
