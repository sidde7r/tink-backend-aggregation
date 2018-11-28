package se.tink.backend.aggregation.agents.brokers.avanza;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.AvanzaV2AccountTypeMappers;
import se.tink.backend.aggregation.rpc.AccountTypes;

public final class AvanzaV2AccountTypeMappersTest {
    @Test
    public void testInferAccountType() {
        AvanzaV2AccountTypeMappers mappers = new AvanzaV2AccountTypeMappers();

        // No warning -- uses product code account type mapper
        Assert.assertEquals(
                Optional.of(AccountTypes.INVESTMENT),
                mappers.inferAccountType("aktie- & fondkonto"));
        Assert.assertEquals(
                Optional.of(AccountTypes.INVESTMENT),
                mappers.inferAccountType("investeringssparkonto"));

        // Warns about using predicate fallback
        Assert.assertEquals(Optional.of(AccountTypes.PENSION), mappers.inferAccountType("pension"));
        Assert.assertEquals(
                Optional.of(AccountTypes.SAVINGS), mappers.inferAccountType("sparkonto"));
        Assert.assertEquals(Optional.of(AccountTypes.LOAN), mappers.inferAccountType("kredit"));

        // Warns about being unable to figure out the type
        Assert.assertEquals(Optional.empty(), mappers.inferAccountType("försäkring"));
    }
}
