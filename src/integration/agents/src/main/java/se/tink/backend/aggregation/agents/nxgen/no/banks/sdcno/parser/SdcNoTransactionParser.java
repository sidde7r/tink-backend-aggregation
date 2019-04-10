package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.parser;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcReservation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.parser.SdcTransactionParser;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;

public class SdcNoTransactionParser implements SdcTransactionParser {

    // convert sdc transaction to Tink transaction for NO market
    @Override
    public Transaction parseTransaction(SdcTransaction bankTransaction) {
        return Transaction.builder()
                .setAmount(bankTransaction.getAmount().toTinkAmount())
                .setDate(DateUtils.parseDate(bankTransaction.getPaymentDate()))
                .setDescription(bankTransaction.getLabel())
                .build();
    }

    @Override
    public Transaction parseTransaction(SdcReservation bankReservation) {
        return Transaction.builder()
                .setAmount(bankReservation.getAmount().toTinkAmount())
                .setDate(DateUtils.parseDate(bankReservation.getCreateDate()))
                .setDescription(bankReservation.getDescription())
                .setPending(true)
                .build();
    }

    // convert sdc credit card transaction to Tink transaction for NO market
    @Override
    public CreditCardTransaction parseCreditCardTransaction(
            CreditCardAccount creditCardAccount, SdcTransaction bankTransaction) {
        return CreditCardTransaction.builder()
                .setAmount(bankTransaction.getAmount().toTinkAmount())
                .setDate(DateUtils.parseDate(bankTransaction.getPaymentDate()))
                .setDescription(bankTransaction.getLabel())
                .setCreditAccount(creditCardAccount)
                .build();
    }

    @Override
    public CreditCardTransaction parseCreditCardTransaction(
            CreditCardAccount creditCardAccount, SdcReservation bankReservation) {
        return CreditCardTransaction.builder()
                .setAmount(bankReservation.getAmount().toTinkAmount())
                .setDate(DateUtils.parseDate(bankReservation.getCreateDate()))
                .setDescription(bankReservation.getDescription())
                .setCreditAccount(creditCardAccount)
                .setPending(true)
                .build();
    }
}
