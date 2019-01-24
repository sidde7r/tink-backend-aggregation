package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.account;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskAccountTypeMappers;
import se.tink.backend.aggregation.rpc.AccountTypes;

public final class BawagPskAccountTypeMappersTest {
    @Test
    public void testInferAccountType() {
        BawagPskAccountTypeMappers mappers = new BawagPskAccountTypeMappers();

        // No warning -- uses product code account type mapper
        Assert.assertEquals(
                Optional.of(AccountTypes.CHECKING),
                mappers.inferAccountType("B131", "CHECKING"));

        // Warns about using predicate fallback
        Assert.assertEquals(
                Optional.of(AccountTypes.LOAN),
                mappers.inferAccountType("S111", "LOAN"));

        // Warns about using product type fallback
        Assert.assertEquals(
                Optional.of(AccountTypes.SAVINGS),
                mappers.inferAccountType("A000", "SAVINGS"));

        // Warns about being unable to figure out the type
        Assert.assertEquals(
                Optional.empty(),
                mappers.inferAccountType("A000", "SPECIAL_BAWAG_TYPE"));
    }
}
