package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.parser;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.parsers.TransactionParser;
import se.tink.libraries.amount.Amount;

public class NordeaDkTransactionParser implements TransactionParser {
    private static final Splitter CLEANUP_SPLITTER =
            Splitter.on(CharMatcher.whitespace()).omitEmptyStrings();
    private static final Joiner CLEANUP_JOINER = Joiner.on(' ');

    @VisibleForTesting
    protected String getRawDescription(TransactionEntity te) {
        return CharMatcher.whitespace()
                .trimFrom(
                        Optional.ofNullable(Strings.emptyToNull(te.getText()))
                                .orElse(te.getCounterPartyName()));
    }

    @Override
    @VisibleForTesting
    public Amount getAmount(TransactionEntity te) {
        return Amount.inDKK(te.getAmount());
    }

    @Override
    @VisibleForTesting
    public Date getDate(TransactionEntity te) {
        return te.getDate();
    }

    @Override
    @VisibleForTesting
    public String getDescription(TransactionEntity te) {
        String text = getRawDescription(te);
        if (Strings.isNullOrEmpty(text)) { // fallback, do not leave description empty
            text = te.getCounterPartyName();
        }
        return CLEANUP_JOINER.join(CLEANUP_SPLITTER.split(text));
    }

    @Override
    @VisibleForTesting
    public boolean isPending(TransactionEntity te) {
        return te.isReservation() != null && te.isReservation();
    }

    @VisibleForTesting
    protected String getRawDescription(PaymentEntity pe) {
        return CharMatcher.whitespace().trimFrom(pe.getBeneficiaryName());
    }

    @Override
    @VisibleForTesting
    public Amount getAmount(PaymentEntity pe) {
        return Amount.inDKK(-AgentParsingUtils.parseAmount(pe.getAmount()));
    }

    @Override
    @VisibleForTesting
    public Date getDate(PaymentEntity pe) {
        return Optional.ofNullable(pe.getPaymentDate())
                .map(
                        date ->
                                AgentParsingUtils.parseDate(
                                        pe.getPaymentDate().substring(0, 10), true))
                .orElse(null);
    }

    @Override
    @VisibleForTesting
    public String getDescription(PaymentEntity pe) {
        return CLEANUP_JOINER.join(CLEANUP_SPLITTER.split(getRawDescription(pe)));
    }
}
