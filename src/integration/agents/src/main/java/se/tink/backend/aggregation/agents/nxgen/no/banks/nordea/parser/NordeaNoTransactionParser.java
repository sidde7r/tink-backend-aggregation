package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.parser;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.parsers.TransactionParser;
import se.tink.libraries.amount.Amount;

public class NordeaNoTransactionParser implements TransactionParser {
    private static final Splitter CLEANUP_SPLITTER =
            Splitter.on(CharMatcher.whitespace()).omitEmptyStrings();
    private static final Joiner CLEANUP_JOINER = Joiner.on(' ');

    @VisibleForTesting
    protected String getRawDescription(TransactionEntity te) {
        return CharMatcher.whitespace().trimFrom(te.getTransactionText());
    }

    @Override
    @VisibleForTesting
    public Amount getAmount(TransactionEntity te) {
        return Amount.inNOK(te.getTransactionAmount());
    }

    @Override
    @VisibleForTesting
    public Date getDate(TransactionEntity te) {
        return te.getTransactionDate();
    }

    @Override
    @VisibleForTesting
    public String getDescription(TransactionEntity te) {
        return CLEANUP_JOINER.join(CLEANUP_SPLITTER.split(getRawDescription(te)));
    }

    @Override
    @VisibleForTesting
    public boolean isPending(TransactionEntity te) {
        return te.isCoverReservationTransaction() != null && te.isCoverReservationTransaction();
    }

    @VisibleForTesting
    protected String getRawDescription(PaymentEntity pe) {
        return CharMatcher.whitespace().trimFrom(pe.getBeneficiaryName());
    }

    @Override
    @VisibleForTesting
    public Amount getAmount(PaymentEntity pe) {
        return Amount.inNOK(-AgentParsingUtils.parseAmount(pe.getAmount()));
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
