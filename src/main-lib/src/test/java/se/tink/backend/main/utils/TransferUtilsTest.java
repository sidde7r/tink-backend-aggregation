package se.tink.backend.main.utils;

import com.google.api.client.util.Lists;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import java.util.List;

import static org.junit.Assert.*;

public class TransferUtilsTest {
    private static final String BARNCANCERFONDEN_ACCOUNT_NUMBER = "9020900";

    private static final AccountIdentifier SWEDISH_IDENTIFIER = new SwedishIdentifier("6152135858358");
    private static final AccountIdentifier SWEDISH_SHB_IDENTIFIER = new SwedishSHBInternalIdentifier("135858358");
    private static final AccountIdentifier FINISH_IDENTIFIER = new FinnishIdentifier("6152135858358");
    private static final AccountIdentifier BARNCANCERFONDEN_BG = new BankGiroIdentifier(BARNCANCERFONDEN_ACCOUNT_NUMBER);
    private static final AccountIdentifier BARNCANCERFONDEN_PG = new PlusGiroIdentifier(BARNCANCERFONDEN_ACCOUNT_NUMBER);

    @Test
    public void testMatches() throws Exception {
        TransferDestinationPattern allSEIdentifiers = makePattern(AccountIdentifier.Type.SE);
        TransferDestinationPattern SEIdentifier = makePattern(SWEDISH_IDENTIFIER);
        TransferDestinationPattern SHBIdentifier = makePattern(SWEDISH_SHB_IDENTIFIER);

        assertTrue(TransferUtils.matches(allSEIdentifiers, SWEDISH_IDENTIFIER));
        assertTrue(TransferUtils.matches(SEIdentifier, SWEDISH_IDENTIFIER));
        assertFalse(TransferUtils.matches(SHBIdentifier, SWEDISH_IDENTIFIER));

        assertFalse(TransferUtils.matches(allSEIdentifiers, SWEDISH_SHB_IDENTIFIER));
        assertFalse(TransferUtils.matches(SEIdentifier, SWEDISH_SHB_IDENTIFIER));
        assertTrue(TransferUtils.matches(SHBIdentifier, SWEDISH_SHB_IDENTIFIER));

        assertFalse(TransferUtils.matches(allSEIdentifiers, FINISH_IDENTIFIER));
        assertFalse(TransferUtils.matches(SEIdentifier, FINISH_IDENTIFIER));
        assertFalse(TransferUtils.matches(SHBIdentifier, FINISH_IDENTIFIER));
    }

    @Test
    public void ensureMatchesReturnsFalse_whenOnlyTypeDoesNotMatch() {
        TransferDestinationPattern pattern = new TransferDestinationPattern();
        pattern.setType(BARNCANCERFONDEN_PG.getType());
        pattern.setPattern(BARNCANCERFONDEN_ACCOUNT_NUMBER);

        assertFalse(TransferUtils.matches(pattern, BARNCANCERFONDEN_BG));
    }

    @Test
    public void ensureMatchesAny_returnsFalse_whenPatterns_areNullOrEmpty() {
        List<TransferDestinationPattern> emptyList = Lists.newArrayList();

        assertFalse(TransferUtils.matchesAny(null, SWEDISH_IDENTIFIER));
        assertFalse(TransferUtils.matchesAny(emptyList, SWEDISH_IDENTIFIER));
    }

    @Test
    public void ensureMatchesAnu_returnsFalse_whenIdentifier_isNull() {
        List<TransferDestinationPattern> patterns = Lists.newArrayList();
        patterns.add(makePattern(AccountIdentifier.Type.SE));
        patterns.add(makePattern(SWEDISH_IDENTIFIER));

        assertFalse(TransferUtils.matchesAny(patterns, null));
    }

    @Test
    public void ensureMatchesAny_returnsTrue_whenPaymentIdentifier_matchPatterns() {
        List<TransferDestinationPattern> paymentPatterns = makePaymentPatterns();

        assertTrue(TransferUtils.matchesAny(paymentPatterns, BARNCANCERFONDEN_BG));
        assertTrue(TransferUtils.matchesAny(paymentPatterns, BARNCANCERFONDEN_PG));
    }

    @Test
    public void ensureMatchesAny_returnsFalse_whenPaymentIdentifier_doesNotMatchPatterns() {
        List<TransferDestinationPattern> patterns = makePaymentPatterns();
        assertFalse(TransferUtils.matchesAny(patterns, SWEDISH_IDENTIFIER));

        patterns = makeTransferPatterns();
        assertFalse(TransferUtils.matchesAny(patterns, SWEDISH_SHB_IDENTIFIER));
        assertFalse(TransferUtils.matchesAny(patterns, FINISH_IDENTIFIER));
    }

   @Test
   public void ensureMatchesAny_returnsTrue_whenSEIdentifier_matchPatterns() {
       List<TransferDestinationPattern> transferPatterns = makeTransferPatterns();
       transferPatterns.add(makePattern(SWEDISH_SHB_IDENTIFIER));

       assertTrue(TransferUtils.matchesAny(transferPatterns, SWEDISH_IDENTIFIER));
       assertTrue(TransferUtils.matchesAny(transferPatterns, SWEDISH_SHB_IDENTIFIER));
   }

    @Test
    public void ensureMatchesAny_returnsFalse_whenSEIdentifier_doesNotMatchPatterns() {
        List<TransferDestinationPattern> transferPatterns = makeTransferPatterns();

        assertFalse(TransferUtils.matchesAny(transferPatterns, FINISH_IDENTIFIER));
        assertFalse(TransferUtils.matchesAny(transferPatterns, BARNCANCERFONDEN_BG));
        assertFalse(TransferUtils.matchesAny(transferPatterns, BARNCANCERFONDEN_PG));
    }

    @Test
    public void ensureFindFirstMatch_returnsAbsent_whenPatterns_areNullOrEmpty() {
        List<TransferDestinationPattern> emptyPatterns = Lists.newArrayList();
        List<AccountIdentifier> identifiers = makeTransferIdentifierList();

        assertFalse(TransferUtils.findFirstMatch(null, identifiers).isPresent());
        assertFalse(TransferUtils.findFirstMatch(emptyPatterns, identifiers).isPresent());
    }

    @Test
    public void ensureFindFirstMatch_returnsAbsent_whenIdentifiers_areNullOrEmpty() {
        List<TransferDestinationPattern> patterns = makeTransferPatterns();
        List<AccountIdentifier> emptyIdentifiers = Lists.newArrayList();

        assertFalse(TransferUtils.findFirstMatch(patterns, null).isPresent());
        assertFalse(TransferUtils.findFirstMatch(patterns, emptyIdentifiers).isPresent());
    }

    @Test
    public void ensureFindFirstMatch_returnsPresent_whenPaymentIdentifier_matchPatterns() {
        List<TransferDestinationPattern> paymentPatterns = makePaymentPatterns();
        List<AccountIdentifier> paymentIdentifiers = makePaymentIdentifierList();

        assertTrue(TransferUtils.findFirstMatch(paymentPatterns, paymentIdentifiers).isPresent());
    }

    @Test
    public void ensureFindFirstMatch_returnsPresent_whenTransferIdentifier_matchPatterns() {
        List<TransferDestinationPattern> transferPatterns = makeTransferPatterns();
        List<AccountIdentifier> transferIdentifiers = makeTransferIdentifierList();

        assertTrue(TransferUtils.findFirstMatch(transferPatterns, transferIdentifiers).isPresent());
    }

    @Test
    public void ensureFindFirstMatch_returnsAbsent_whenPaymentIdentifiers_doesNotMatchPatterns() {
        List<TransferDestinationPattern> transferPatterns = makeTransferPatterns();
        List<AccountIdentifier> paymentIdentifiers = makePaymentIdentifierList();

        assertFalse(TransferUtils.findFirstMatch(transferPatterns, paymentIdentifiers).isPresent());
    }

    @Test
    public void ensureFindFirstMatch_returnsAbsent_whenTransferIdentifiers_doesNotMatchPatterns() {
        List<TransferDestinationPattern> paymentPatterns = makePaymentPatterns();
        List<AccountIdentifier> transferIdentifiers = makeTransferIdentifierList();

        assertFalse(TransferUtils.findFirstMatch(paymentPatterns, transferIdentifiers).isPresent());
    }

    private List<AccountIdentifier> makeTransferIdentifierList() {
        List<AccountIdentifier> identifiers = Lists.newArrayList();

        identifiers.add(SWEDISH_IDENTIFIER);
        identifiers.add(SWEDISH_SHB_IDENTIFIER);
        identifiers.add(FINISH_IDENTIFIER);

        return identifiers;
    }

    private List<AccountIdentifier> makePaymentIdentifierList() {
        List<AccountIdentifier> identifiers = Lists.newArrayList();

        identifiers.add(BARNCANCERFONDEN_BG);
        identifiers.add(BARNCANCERFONDEN_PG);

        return identifiers;
    }

    private List<TransferDestinationPattern> makeTransferPatterns() {
        List<TransferDestinationPattern> patterns = Lists.newArrayList();
        patterns.add(makePattern(AccountIdentifier.Type.SE));
        patterns.add(makePattern(SWEDISH_IDENTIFIER));

        return patterns;
    }

    private List<TransferDestinationPattern> makePaymentPatterns() {
        List<TransferDestinationPattern> patterns = Lists.newArrayList();
        patterns.add(makePattern(AccountIdentifier.Type.SE_BG));
        patterns.add(makePattern(AccountIdentifier.Type.SE_PG));

        return patterns;
    }

    private TransferDestinationPattern makePattern(AccountIdentifier.Type type) {
        TransferDestinationPattern tdp = new TransferDestinationPattern();
        tdp.setType(type);
        tdp.setPattern(".+");

        return tdp;
    }

    private TransferDestinationPattern makePattern(AccountIdentifier identifier) {
        TransferDestinationPattern tdp = new TransferDestinationPattern();
        tdp.setType(identifier.getType());
        tdp.setPattern(identifier.getIdentifier());

        return tdp;
    }
}
