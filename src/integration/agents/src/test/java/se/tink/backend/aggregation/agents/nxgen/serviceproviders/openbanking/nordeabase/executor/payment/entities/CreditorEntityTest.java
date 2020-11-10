package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class CreditorEntityTest {

    private static final String CURRENCY = "SEK";
    private static final SwedishIdentifier SOURCE_ACCOUNT =
            (SwedishIdentifier) AccountIdentifier.create(Type.SE, "6666123456789", "Sven Svensson");
    private static final SwedishIdentifier DESTINATION_ACCOUNT_SE =
            (SwedishIdentifier) AccountIdentifier.create(Type.SE, "66661111111", "Test Testsson");
    private static final BankGiroIdentifier ACCOUNT_BG =
            (BankGiroIdentifier) AccountIdentifier.create(Type.SE_BG, "51707560", "Test Company");

    private static final String TINK_TEST = "TinkTest";
    private static final String VALID_OCR = "202091953424";
    private static final String OCR = "OCR";

    @Test
    public void testCreditorEntityIfMessageIsPresentForBankTransferDomesticPayment()
            throws PaymentException {

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(TINK_TEST);
        PaymentRequest request =
                paymentRequest(
                        SOURCE_ACCOUNT,
                        DESTINATION_ACCOUNT_SE,
                        LocalDate.now().plusDays(1),
                        remittanceInformation);
        CreditorEntity creditorEntity = CreditorEntity.of(request);
        Assert.assertNotNull(creditorEntity.getMessage());
        Assert.assertNull(creditorEntity.getReference());
        Assert.assertEquals(creditorEntity.getMessage(), TINK_TEST);
    }

    @Test
    public void testCreditorEntityMessageIsPresentForBankGiroDomesticPayment()
            throws PaymentException {

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(TINK_TEST);
        PaymentRequest request =
                paymentRequest(
                        SOURCE_ACCOUNT,
                        ACCOUNT_BG,
                        LocalDate.now().plusDays(1),
                        remittanceInformation);
        CreditorEntity creditorEntity = CreditorEntity.of(request);
        Assert.assertNotNull(creditorEntity.getMessage());
        Assert.assertNull(creditorEntity.getReference());
        Assert.assertEquals(creditorEntity.getMessage(), TINK_TEST);
    }

    @Test
    public void testCreditorEntityOCRIsPresentForBankGiroDomesticPayment() throws PaymentException {

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(VALID_OCR);
        remittanceInformation.setType(RemittanceInformationType.OCR);
        PaymentRequest request =
                paymentRequest(
                        SOURCE_ACCOUNT,
                        ACCOUNT_BG,
                        LocalDate.now().plusDays(1),
                        remittanceInformation);
        CreditorEntity creditorEntity = CreditorEntity.of(request);
        Assert.assertNull(creditorEntity.getMessage());
        Assert.assertNotNull(creditorEntity.getReference());
        Assert.assertEquals(creditorEntity.getReference().toTinkReference().getValue(), VALID_OCR);
        Assert.assertEquals(creditorEntity.getReference().toTinkReference().getType(), OCR);
    }

    private PaymentRequest paymentRequest(
            AccountIdentifier fromAccount,
            AccountIdentifier toAccount,
            LocalDate executionDate,
            RemittanceInformation remittanceInformation) {
        return new PaymentRequest(
                new Payment.Builder()
                        .withCreditor(new Creditor(toAccount, toAccount.getName().orElse(null)))
                        .withDebtor(new Debtor(fromAccount))
                        .withExactCurrencyAmount(ExactCurrencyAmount.of(BigDecimal.ONE, CURRENCY))
                        .withExecutionDate(executionDate)
                        .withCurrency(CURRENCY)
                        .withType(PaymentType.DOMESTIC)
                        .withRemittanceInformation(remittanceInformation)
                        .build());
    }
}
