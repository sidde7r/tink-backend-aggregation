package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.util;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.dto.PaymentInitiationRequestParams;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.enums.EnterCardAccountType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.enums.EnterCardPaymentRequestType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public final class EnterCardPaymentUtil {

    public static PaymentInitiationRequestParams getPaymentInitiationReqParams(
            AccountIdentifier.Type creditorAccountType,
            AccountIdentifier.Type debtorAccountType,
            String creditorAccountNumber) {

        Long clearingNumber = null;
        Long ocrNumber = null;
        EnterCardPaymentRequestType requestType = EnterCardPaymentRequestType.FT;
        EnterCardAccountType accountType = EnterCardAccountType.BANK_ACCOUNT;

        if (creditorAccountType.equals(debtorAccountType)) {
            requestType = EnterCardPaymentRequestType.BT;
        }

        if (Type.SE.equals(creditorAccountType)) {
            SwedishIdentifier identifier = new SwedishIdentifier(creditorAccountNumber);
            clearingNumber = Long.parseLong(identifier.getClearingNumber());
        }

        if (Type.SE_BG.equals(creditorAccountType)) {
            BankGiroIdentifier bankGiroIdentifier = new BankGiroIdentifier(creditorAccountNumber);
            ocrNumber = Long.parseLong(bankGiroIdentifier.getOcr().get());
            requestType = EnterCardPaymentRequestType.BP;
            accountType = EnterCardAccountType.BANK_GIRO;
        }

        if (Type.SE_PG.equals(creditorAccountType)) {
            PlusGiroIdentifier plusGiroIdentifier = new PlusGiroIdentifier(creditorAccountNumber);
            ocrNumber = Long.parseLong(plusGiroIdentifier.getOcr().get());
            requestType = EnterCardPaymentRequestType.BP;
            accountType = EnterCardAccountType.PLUS_GIRO;
        }

        return new PaymentInitiationRequestParams(
                clearingNumber, ocrNumber, requestType, accountType);
    }
}
