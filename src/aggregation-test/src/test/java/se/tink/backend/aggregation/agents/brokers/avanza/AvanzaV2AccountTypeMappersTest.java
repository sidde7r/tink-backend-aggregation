package se.tink.backend.aggregation.agents.brokers.avanza;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;

public final class AvanzaV2AccountTypeMappersTest {
    @Test
    public void testInferExactAccountType() {
        AvanzaV2AccountTypeMappers mappers = new AvanzaV2AccountTypeMappers();

        // No warning -- uses exact mapper
        Assert.assertEquals(
                Optional.of(AccountTypes.INVESTMENT),
                mappers.inferAccountType("AktieFondkonto"));
        Assert.assertEquals(
                Optional.of(AccountTypes.INVESTMENT),
                mappers.inferAccountType("Investeringssparkonto"));
        Assert.assertEquals(
                Optional.of(AccountTypes.INVESTMENT),
                mappers.inferAccountType("Kapitalforsakring"));
        Assert.assertEquals(
                Optional.of(AccountTypes.SAVINGS),
                mappers.inferAccountType("Sparkonto"));
        Assert.assertEquals(
                Optional.of(AccountTypes.SAVINGS),
                mappers.inferAccountType("SparkontoPlus"));
        Assert.assertEquals(
                Optional.of(AccountTypes.PENSION),
                mappers.inferAccountType("Tjanstepension"));
    }

    @Test
    public void testInferFallbackAccountType() {
        AvanzaV2AccountTypeMappers mappers = new AvanzaV2AccountTypeMappers();

        // Warns about using fallback mapper
        Assert.assertEquals(Optional.of(AccountTypes.PENSION), mappers.inferAccountType("pension"));
        Assert.assertEquals(
                Optional.of(AccountTypes.SAVINGS), mappers.inferAccountType("sparkonto"));
        Assert.assertEquals(Optional.of(AccountTypes.LOAN), mappers.inferAccountType("kredit"));
    }

    @Test
    public void testInferMissingAccountType() {
        AvanzaV2AccountTypeMappers mappers = new AvanzaV2AccountTypeMappers();

        // Warns about being unable to figure out the type
        Assert.assertEquals(Optional.empty(), mappers.inferAccountType("försäkring"));
    }
}
