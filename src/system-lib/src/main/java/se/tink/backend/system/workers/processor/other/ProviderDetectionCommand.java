package se.tink.backend.system.workers.processor.other;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;

public class ProviderDetectionCommand implements TransactionProcessorCommand {

    private static final double MINIMUM_AMOUNT = 1000;
    private static final ImmutableMap<Pattern, String> PATTERNS;

    static {
        Builder<Pattern, String> patternBuilder = ImmutableMap.builder();
        
        // Travel
        addPattern(patternBuilder, "(sas|eurobon).*(amex|america.*expr)", "saseurobonusamericanexpress");
        addPattern(patternBuilder, "(sas|eurobon).*(master|card)", "saseurobonusmastercard");
        addPattern(patternBuilder, "sj.*(prio|master|card)", "sjpriomastercard");
        addPattern(patternBuilder, "finn.*air.*(master|card)", "finnairmastercard");
        addPattern(patternBuilder, "(nordic)?( )?choice.*(master|card)", "choicemastercard");

        // Gas stations
        addPattern(patternBuilder, "jet.*(master|card)", "jetmastercard");
        addPattern(patternBuilder, "ok.*q8.*(bank|visa)", "okq8bank");
        addPattern(patternBuilder, "shell.*(master|card)", "shellmastercard");
        addPattern(patternBuilder, "statoil.*(master|card)", "statoilmastercard");
        addPattern(patternBuilder, "preem.*(privat|kort|master|card)", "preem");
        addPattern(patternBuilder, "(volkswagen|vw).*(visa|kort)", "volkswagenkortet");
        addPattern(patternBuilder, "seat.*(visa|kort)", "seatkortet");
        addPattern(patternBuilder, "skoda.*(visa|kort)", "skodakortet");

        // Commerce
        addPattern(patternBuilder, "(^| )ica( )?(kredit|bank|master|maestro)", "icabanken");
        addPattern(patternBuilder, "^coop ((med( )?mera )|visa|kort)", "coop");
        addPattern(patternBuilder, "(^| )nk( )?(nyckel|kort|master|card)", "nknyckelnmastercard");

        // Brokers
        addPattern(patternBuilder, "avanza", "avanza");
        addPattern(patternBuilder, "nordnet", "nordnet");
        addPattern(patternBuilder, "(lysa|.* lysa)", "lysa");

        // Other
        addPattern(patternBuilder, "(^| )csn(-| |$)", "csn");

        // Banks
        addPattern(patternBuilder, "nordea.*(kredit|kort|master|card|silver|gold|platinum|premium|black)", "nordea");
        addPattern(patternBuilder, "swedbank.*(kredit|kort|master|card)", "swedbank");
        addPattern(patternBuilder, "seb.*(kredit|euro|master|card)", "seb");
        addPattern(patternBuilder, "danske( )?(bank)?.*(kredit|kort|master|card|direkt|guld|platinum)", "danskebank");
        addPattern(patternBuilder, "((handelsbanken.*(kredit|kort|master|card|platinum))|allkort)", "handelsbanken");
        addPattern(patternBuilder, "l.{1}nsf.{1}rs.{1}kringar.*(kredit|kort|visa)", "lansforsakringar");
        addPattern(patternBuilder, "skandia.*(kredit|kort|visa)", "skandiabanken");
        addPattern(patternBuilder, "sp.*resund.*(kredit|kort|master|card)", "sparbankenoresund-bankid");
        addPattern(patternBuilder, "sp.*syd.*(kredit|kort|master|card)", "sparbankensyd-bankid");

        // Credit cards
        addPattern(patternBuilder, "(amex|america.*expr)", "americanexpress");
        addPattern(patternBuilder, "euro.*card", "eurocard");
        addPattern(patternBuilder, "riks.*kortet", "rikskortet");
        addPattern(patternBuilder, "supr.*card", "supremecard");
        
        PATTERNS = patternBuilder.build();
    }

    @VisibleForTesting
    /*package*/ ProviderDetectionCommand() {
        // Deliberately left empty.
    }

    /**
     * Build a {@link ProviderDetectionCommand}. Returns {@link Optional#empty()} if not relevant for the given
     * provider.
     *
     * @param provider the provider delivering new transactions
     * @return a command if relevant for the provider, empty Optional otherwise.
     */
    public static Optional<ProviderDetectionCommand> build(Provider provider) {
        if (!"SE".equals(provider.getMarket())) {
            return Optional.empty();
        }
        return Optional.of(new ProviderDetectionCommand());
    }

    private static void addPattern(Builder<Pattern,String> patternBuilder, String pattern, String providername) {
        patternBuilder.put(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE), providername);
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {

        double amount = transaction.getAmount();

        // TODO: Implement currencyFactor (and divide the current threshold with 10)

        // Process only expenses with large enough amounts
        if (amount < 0 && Math.abs(amount) > MINIMUM_AMOUNT) {
            String transferAccount = transaction.getPayloadValue(TransactionPayloadTypes.TRANSFER_ACCOUNT);
            // Process only transactions that aren't already paired with connected accounts
            if (Strings.isNullOrEmpty(transferAccount)) {
                String description = transaction.getDescription();
                // Test all patterns on the description
                for (Pattern pattern : PATTERNS.keySet()) {
                    Matcher matcher = pattern.matcher(description);
                    if (matcher.find()) {
                        transaction.setPayload(TransactionPayloadTypes.TRANSFER_PROVIDER, PATTERNS.get(pattern));
                        break;
                    }
                }
            }
        }

        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
        // Deliberately left empty.
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
