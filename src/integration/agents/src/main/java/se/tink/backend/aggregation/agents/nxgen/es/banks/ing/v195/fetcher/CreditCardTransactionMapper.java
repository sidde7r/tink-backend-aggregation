package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.IngElement;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

public class CreditCardTransactionMapper implements IngTransactionMapper<CreditCardAccount> {

    @Override
    public Transaction toTinkTransaction(CreditCardAccount account, IngElement rawTransaction) {
        return (CreditCardTransaction)
                CreditCardTransaction.builder()
                        .setCreditAccount(account)
                        .setAmount(
                                ExactCurrencyAmount.of(
                                        rawTransaction.getAmount(),
                                        account.getExactBalance().getCurrencyCode()))
                        .setDate(rawTransaction.getDate())
                        .setDescription(rawTransaction.getDescription())
                        .setMutable(false)
                        .setMutable(false)
                        .setTransactionDates(
                                TransactionDates.builder()
                                        .setBookingDate(
                                                new AvailableDateInformation(
                                                        rawTransaction.getDate()))
                                        .setValueDate(
                                                new AvailableDateInformation(
                                                        rawTransaction.getDate()))
                                        .build())
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                rawTransaction.getUuid())
                        .setProviderMarket(MarketCode.ES.toString())
                        .build();
    }
}
