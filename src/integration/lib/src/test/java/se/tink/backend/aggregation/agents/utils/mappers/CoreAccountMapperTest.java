package se.tink.backend.aggregation.agents.utils.mappers;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.enums.BalanceType;
import se.tink.libraries.account.enums.CreditLineType;
import se.tink.libraries.account.rpc.Account;
import se.tink.libraries.account.rpc.Balance;
import se.tink.libraries.account.rpc.CreditLine;
import se.tink.libraries.amount.ExactCurrencyAmount;

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

        se.tink.backend.agents.rpc.CreditLine cl = new se.tink.backend.agents.rpc.CreditLine();
        cl.setType(se.tink.backend.agents.rpc.CreditLineType.TMP_POC_EXAMPLE);
        cl.setIncludedInBalance(true);
        cl.setAmount(new ExactCurrencyAmount(new BigDecimal(BigInteger.valueOf(1000), 2), "SEK"));
        se.tink.backend.agents.rpc.Balance balance = new se.tink.backend.agents.rpc.Balance();
        balance.setCreditLines(ImmutableList.of(cl));
        balance.setType(se.tink.backend.agents.rpc.BalanceType.AVAILABLE_BALANCE);
        balance.setAmount(
                new ExactCurrencyAmount(new BigDecimal(BigInteger.valueOf(2000), 2), "SEK"));
        account.setBalances(ImmutableList.of(balance));

        Account coreAccount = CoreAccountMapper.fromAggregation(account);

        assertEquals("credentialsId", coreAccount.getCredentialsId());
        assertEquals("Thomas Alan Waits", coreAccount.getHolderName());
        assertEquals(ImmutableList.of(AccountFlag.BUSINESS), coreAccount.getFlags());

        assertEquals(1, coreAccount.getBalances().size());
        Balance coreBalance = coreAccount.getBalances().get(0);
        assertEquals(2000, coreBalance.getAmount().getUnscaledValue());
        assertEquals(2, coreBalance.getAmount().getScale());
        assertEquals("SEK", coreBalance.getAmount().getCurrencyCode());
        assertEquals(BalanceType.AVAILABLE_BALANCE, coreBalance.getType());

        assertEquals(1, coreBalance.getCreditLines().size());
        CreditLine coreCreditLine = coreBalance.getCreditLines().get(0);
        assertEquals(1000, coreCreditLine.getAmount().getUnscaledValue());
        assertEquals(2, coreCreditLine.getAmount().getScale());
        assertEquals("SEK", coreCreditLine.getAmount().getCurrencyCode());
        assertEquals(CreditLineType.TMP_POC_EXAMPLE, coreCreditLine.getType());
    }
}
