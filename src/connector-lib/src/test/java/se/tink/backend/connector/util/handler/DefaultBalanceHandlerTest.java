package se.tink.backend.connector.util.handler;

import com.google.common.collect.Maps;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.connector.rpc.PartnerAccountPayload;
import se.tink.backend.core.Account;

public class DefaultBalanceHandlerTest {

    private BalanceHandler balanceHandler;
    private static final double EPSILON = 0.0001;

    @Before
    public void setUp() {
        balanceHandler = new DefaultBalanceHandler(null);
    }

    @Test
    public void testGetBalance() throws Exception {
        Account account = new Account();

        Map<String, Object> emptyPayload = Maps.newHashMap();
        Map<String, Object> ignoreBalancePayload = Maps.newHashMap();
        ignoreBalancePayload.put(PartnerAccountPayload.IGNORE_BALANCE, true);

        // Debit accounts
        balanceHandler.setNewBalance(account, 2000d, 7000d, emptyPayload);
        Assert.assertEquals(5000d, account.getBalance(), EPSILON);
        balanceHandler.setNewBalance(account, 0d, 7000d, emptyPayload);
        Assert.assertEquals(7000d, account.getBalance(), EPSILON);
        balanceHandler.setNewBalance(account, null, 7000d, emptyPayload);
        Assert.assertEquals(7000d, account.getBalance(), EPSILON);

        // Credit cards
        balanceHandler.setNewBalance(account, 1000d, -4000d, emptyPayload);
        Assert.assertEquals(-5000d, account.getBalance(), EPSILON);
        balanceHandler.setNewBalance(account, 0d, -4000d, emptyPayload);
        Assert.assertEquals(-4000d, account.getBalance(), EPSILON);
        balanceHandler.setNewBalance(account, null, -4000d, emptyPayload);
        Assert.assertEquals(-4000d, account.getBalance(), EPSILON);

        // Ignore balance case
        account.setBalance(100);
        balanceHandler.setNewBalance(account, null, -4000d, ignoreBalancePayload);
        Assert.assertEquals(100, account.getBalance(), 0);
        balanceHandler.setNewBalance(account, 2000d, -4000d, ignoreBalancePayload);
        Assert.assertEquals(100, account.getBalance(), 0);
    }
}
