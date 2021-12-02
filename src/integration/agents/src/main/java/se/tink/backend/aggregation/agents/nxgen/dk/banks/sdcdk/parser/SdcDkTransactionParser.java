package se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk.parser;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcReservation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.parser.SdcTransactionParser;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;

public class SdcDkTransactionParser implements SdcTransactionParser {

    // convert sdc transaction to Tink transaction for DK market
    @Override
    public Transaction parseTransaction(SdcTransaction bankTransaction) {
        return Transaction.builder()
                .setAmount(bankTransaction.getAmount().toExactCurrencyAmount())
                .setDate(DateUtils.parseDate(bankTransaction.getPaymentDate()))
                .setDescription(bankTransaction.getLabel())
                .build();
    }

    @Override
    public Transaction parseTransaction(SdcReservation bankReservation) {
        return Transaction.builder()
                .setAmount(bankReservation.getAmount().toExactCurrencyAmount())
                .setDate(DateUtils.parseDate(bankReservation.getCreateDate()))
                .setDescription(bankReservation.getDescription())
                .setPending(true)
                .build();
    }

    @Override
    public CreditCardTransaction parseCreditCardTransaction(
            CreditCardAccount creditCardAccount, SdcTransaction bankTransaction) {
        return CreditCardTransaction.builder()
                .setAmount(bankTransaction.getAmount().toExactCurrencyAmount())
                .setDate(DateUtils.parseDate(bankTransaction.getPaymentDate()))
                .setDescription(bankTransaction.getLabel())
                .setCreditCardAccountNumber(
                        creditCardAccount != null ? creditCardAccount.getAccountNumber() : null)
                .build();
    }

    @Override
    public CreditCardTransaction parseCreditCardTransaction(
            CreditCardAccount creditCardAccount, SdcReservation bankReservation) {
        return CreditCardTransaction.builder()
                .setAmount(bankReservation.getAmount().toExactCurrencyAmount())
                .setDate(DateUtils.parseDate(bankReservation.getCreateDate()))
                .setDescription(bankReservation.getDescription())
                .setCreditCardAccountNumber(
                        creditCardAccount != null ? creditCardAccount.getAccountNumber() : null)
                .setPending(true)
                .build();
    }
}
