package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis;

import se.tink.libraries.payment.rpc.Reference;

public class UkOpenBankingV31PisUtils {

    /**
     * Reference type should be type of reference (OCR, free text etc.) but is instead the transfer
     * type according to how the payment controller was designed. The type will always be
     * BANK_TRANSFER for UKOB. It's hardcoded here to construct the Reference correctly according to
     * the initial PaymentRequest that is sent to the agent.
     */
    public static Reference createTinkReference(String reference) {
        return new Reference("BANK_TRANSFER", reference);
    }
}
