package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.parser;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.parsers.TransactionParser;
import se.tink.backend.core.Amount;

public class NordeaFiTransactionParser implements TransactionParser {

    @Override
    public Date getDate(TransactionEntity transactionEntity) {
        return transactionEntity.getDate();
    }

    @Override
    public String getDescription(TransactionEntity transactionEntity) {
        String description = !Strings.isNullOrEmpty(transactionEntity.getCounterPartyName()) ?
                transactionEntity.getCounterPartyName() : transactionEntity.getText();

        return CharMatcher.WHITESPACE.trimFrom(description);
    }

    public Amount getAmount(TransactionEntity transactionEntity) {
        return new Amount(transactionEntity.getCurrency(), transactionEntity.getAmount());
    }

    @Override
    public boolean isPending(TransactionEntity transactionEntity) {
        if (transactionEntity instanceof CreditCardTransactionEntity) {
            CreditCardTransactionEntity tx = (CreditCardTransactionEntity) transactionEntity;
            return transactionEntity.isReservation() || (tx.isBilled() != null && !tx.isBilled());
        }
        return transactionEntity.isReservation();
    }

    public Amount getAmount(PaymentEntity paymentEntity) {
        return Amount.inEUR(-AgentParsingUtils.parseAmount(paymentEntity.getAmount()));
    }

    @Override
    public Date getDate(PaymentEntity paymentEntity) {
        return Optional.ofNullable(paymentEntity.getPaymentDate())
                .map(date -> AgentParsingUtils.parseDate(paymentEntity.getPaymentDate().substring(0, 10), true))
                .orElse(null);
    }

    @Override
    public String getDescription(PaymentEntity paymentEntity) {
        return CharMatcher.WHITESPACE.trimFrom(paymentEntity.getBeneficiaryName());
    }
}
