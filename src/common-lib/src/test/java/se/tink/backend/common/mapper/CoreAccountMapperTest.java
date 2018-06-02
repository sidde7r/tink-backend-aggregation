package se.tink.backend.common.mapper;

import org.junit.Test;
import se.tink.backend.core.Account;
import static org.junit.Assert.assertEquals;

public class CoreAccountMapperTest {
    @Test
    public void allFieldsMappedToAggregation() {
        CoreAccountMapper.toAggregationTypeMap.validate();
    }

    @Test
    public void mappableToAggregation() {
        Account account = new Account();
        account.setAccountNumber("accountNumber");
        account.setHolderName("Thomas Alan Waits");

        se.tink.backend.aggregation.rpc.Account aggregationAccount = CoreAccountMapper.toAggregation(account);

        assertEquals("accountNumber", aggregationAccount.getAccountNumber());
        assertEquals("Thomas Alan Waits", aggregationAccount.getHolderName());
    }

    @Test
    public void allFieldsMappedFromAggregation() {
        CoreAccountMapper.fromAggregationTypeMap.validate();
    }

    @Test
    public void mappableFromAggregation() {
        se.tink.backend.aggregation.rpc.Account account = new se.tink.backend.aggregation.rpc.Account();
        account.setCredentialsId("credentialsId");
        account.setHolderName("Thomas Alan Waits");

        Account coreAccount = CoreAccountMapper.fromAggregation(account);

        assertEquals("credentialsId", coreAccount.getCredentialsId());
        assertEquals("Thomas Alan Waits", coreAccount.getHolderName());
    }

}
