package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.parser;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcReservation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcTransaction;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public interface SdcTransactionParser {
    Transaction parseTransaction(SdcTransaction bankTransaction);
    Transaction parseTransaction(SdcReservation bankReservation);
    CreditCardTransaction parseCreditCardTransaction(CreditCardAccount creditCardAccount, SdcTransaction bankTransaction);
    CreditCardTransaction parseCreditCardTransaction(CreditCardAccount creditCardAccount, SdcReservation bankReservation);
}
