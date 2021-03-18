package se.tink.backend.aggregation.agents.general;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.swedbank.SwedbankClearingNumberUtils;

public class TransferDestinationPatternBuilder {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Collection<Account> tinkAccounts;
    private List<? extends GeneralAccountEntity> destinationAccounts;
    private List<? extends GeneralAccountEntity> sourceAccounts;

    private List<TransferDestinationPattern> multiMatchPatterns;

    private AccountIdentifierType destinationAccountType;
    private Class<? extends AccountIdentifier> destinationAccountIdentifier;

    public TransferDestinationPatternBuilder() {
        // Defaults
        destinationAccountType = AccountIdentifierType.SE;
        destinationAccountIdentifier = SwedishIdentifier.class;
    }

    public TransferDestinationPatternBuilder setSourceAccounts(
            List<? extends GeneralAccountEntity> sourceAccounts) {
        this.sourceAccounts = sourceAccounts;
        return this;
    }

    public TransferDestinationPatternBuilder setDestinationAccounts(
            List<? extends GeneralAccountEntity> destinationAccounts) {
        this.destinationAccounts = destinationAccounts;
        return this;
    }

    public TransferDestinationPatternBuilder setTinkAccounts(Collection<Account> tinkAccounts) {
        this.tinkAccounts = tinkAccounts;
        return this;
    }

    public TransferDestinationPatternBuilder matchDestinationAccountsOn(
            AccountIdentifierType destinationAccountType,
            Class<? extends AccountIdentifier> destinationAccountIdentifier) {

        this.destinationAccountType = destinationAccountType;
        this.destinationAccountIdentifier = destinationAccountIdentifier;
        return this;
    }

    public TransferDestinationPatternBuilder addMultiMatchPattern(
            AccountIdentifierType type, String pattern) {
        if (multiMatchPatterns == null) {
            multiMatchPatterns = Lists.newArrayList();
        }
        multiMatchPatterns.add(TransferDestinationPattern.createForMultiMatch(type, pattern));
        return this;
    }

    public Map<Account, List<TransferDestinationPattern>> build() {

        Preconditions.checkNotNull(destinationAccounts);
        Preconditions.checkNotNull(sourceAccounts);
        Preconditions.checkNotNull(tinkAccounts);

        List<TransferDestinationPattern> validPatterns = Lists.newArrayList();

        if (multiMatchPatterns != null) {
            for (TransferDestinationPattern multiMatchPattern : multiMatchPatterns) {
                validPatterns.add(multiMatchPattern);
            }
        }

        // Also add those already known accounts for transfers without bankid sign
        for (GeneralAccountEntity gae : destinationAccounts) {

            AccountIdentifier identifier = gae.generalGetAccountIdentifier();

            if (identifier.isValid()) {
                if (SwedbankClearingNumberUtils.isSwedbank8xxxAccountNumber(identifier)) {
                    identifier =
                            SwedbankClearingNumberUtils.removeZerosBetweenClearingAndAccountNumber(
                                    identifier.to(SwedishIdentifier.class));
                }

                String name = gae.generalGetName();
                String bank = gae.generalGetBank();
                validPatterns.add(
                        TransferDestinationPattern.createForSingleMatch(identifier, name, bank));
            } else {
                logger.warn("Found non-valid destination: " + getCallingMethod());
            }
        }

        Map<Account, List<TransferDestinationPattern>> result = Maps.newHashMap();
        for (Account tinkAccount : tinkAccounts) {
            AccountIdentifier identifier =
                    tinkAccount.getIdentifier(destinationAccountType, destinationAccountIdentifier);

            // Validate we can transfer from this account.
            if (identifier != null && identifier.isValid()) {
                if (GeneralUtils.isAccountExisting(identifier, sourceAccounts)) {

                    // Filter away the source account from the valid destination
                    List<TransferDestinationPattern> filtered =
                            getValidPatternsForIdentifer(identifier, validPatterns);

                    // The current tink account is a valid source account for transfers
                    result.put(tinkAccount, filtered);
                }
            }
        }
        return result;
    }

    private String getCallingMethod() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String msg = "";
        if (stackTrace.length > 3) {
            msg = stackTrace[3].getClassName() + "." + stackTrace[3].getMethodName();
        }

        return msg;
    }

    private List<TransferDestinationPattern> getValidPatternsForIdentifer(
            final AccountIdentifier identifier, List<TransferDestinationPattern> validPatterns) {

        return Lists.newArrayList(
                Iterables.filter(
                        validPatterns,
                        pattern -> {
                            if (pattern.getType() != identifier.getType()) {
                                // Shouldn't be filtered away -- Not the same type
                                return true;
                            }

                            if (pattern.isMatchesMultiple()) {
                                // Shouldn't be filtered away -- Matches multiple
                                return true;
                            }

                            if (!identifier.getIdentifier().equals(pattern.getPattern())) {
                                // Shouldn't be filtered away -- Not the same identifier
                                return true;
                            }

                            // Filter away -- This pattern is the same as the identifier
                            return false;
                        }));
    }
}
