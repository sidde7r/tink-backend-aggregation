package se.tink.backend.aggregation.agents.utils.mappers;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.rpc.Account;

public class CoreAccountMapperTest {
    @Test
    public void allFieldsMappedToAggregation() {
        CoreAccountMapper.toAggregationTypeMap
                .addMappings(
                        mapper -> mapper.skip(se.tink.backend.agents.rpc.Account::setExactBalance))
                .addMappings(
                        mapper ->
                                mapper.skip(
                                        se.tink.backend.agents.rpc.Account
                                                ::setExactAvailableCredit))
                .addMappings(
                        mapper -> mapper.skip(se.tink.backend.agents.rpc.Account::setCapabilities))
                .addMappings(
                        mapper -> mapper.skip(se.tink.backend.agents.rpc.Account::setSourceInfo))
                .addMappings(
                        mapper -> mapper.skip(se.tink.backend.agents.rpc.Account::setAccountHolder))
                .validate();
    }

    @Test
    public void mappableToAggregation() {
        Account account = new Account();
        account.setAccountNumber("accountNumber");
        account.setHolderName("Thomas Alan Waits");
        account.setFlags(ImmutableList.of(AccountFlag.BUSINESS));

        se.tink.backend.agents.rpc.Account aggregationAccount =
                CoreAccountMapper.toAggregation(account);

        assertEquals("accountNumber", aggregationAccount.getAccountNumber());
        assertEquals("Thomas Alan Waits", aggregationAccount.getHolderName());
        assertEquals(ImmutableList.of(AccountFlag.BUSINESS), aggregationAccount.getFlags());
    }

    @Test
    public void allFieldsMappedFromAggregation() {
        CoreAccountMapper.fromAggregationTypeMap.validate();
    }

    @Test
    public void mappableFromAggregation() {
        se.tink.backend.agents.rpc.Account account = new se.tink.backend.agents.rpc.Account();
        account.setCredentialsId("credentialsId");
        account.setHolderName("Thomas Alan Waits");
        account.setFlags(ImmutableList.of(AccountFlag.BUSINESS));

        Account coreAccount = CoreAccountMapper.fromAggregation(account);

        assertEquals("credentialsId", coreAccount.getCredentialsId());
        assertEquals("Thomas Alan Waits", coreAccount.getHolderName());
        assertEquals(ImmutableList.of(AccountFlag.BUSINESS), coreAccount.getFlags());
    }
}
