package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;

public final class AvanzaAccountTypeMappersTest {
    @Test
    public void testInferExactAccountType() {
        AvanzaAccountTypeMappers mappers = new AvanzaAccountTypeMappers();

        // No warning -- uses product code account type mapper
        Assert.assertEquals(
                Optional.of(AccountTypes.INVESTMENT), mappers.inferAccountType("AktieFondkonto"));
        Assert.assertEquals(
                Optional.of(AccountTypes.INVESTMENT),
                mappers.inferAccountType("Investeringssparkonto"));
        Assert.assertEquals(
                Optional.of(AccountTypes.INVESTMENT),
                mappers.inferAccountType("Kapitalforsakring"));
        Assert.assertEquals(
                Optional.of(AccountTypes.SAVINGS), mappers.inferAccountType("Sparkonto"));
        Assert.assertEquals(
                Optional.of(AccountTypes.SAVINGS), mappers.inferAccountType("SparkontoPlus"));
        Assert.assertEquals(
                Optional.of(AccountTypes.PENSION), mappers.inferAccountType("Tjanstepension"));
    }

    @Test
    public void testInferFallbackAccountType() {
        AvanzaAccountTypeMappers mappers = new AvanzaAccountTypeMappers();
        // Warns about using predicate fallback
        Assert.assertEquals(Optional.of(AccountTypes.PENSION), mappers.inferAccountType("pension"));
        Assert.assertEquals(
                Optional.of(AccountTypes.SAVINGS), mappers.inferAccountType("sparkonto"));
        Assert.assertEquals(Optional.of(AccountTypes.LOAN), mappers.inferAccountType("kredit"));
    }

    @Test
    public void testInferMissingAccountType() {
        AvanzaAccountTypeMappers mappers = new AvanzaAccountTypeMappers();
        // Warns about being unable to figure out the type
        Assert.assertEquals(Optional.empty(), mappers.inferAccountType("försäkring"));
    }
}
