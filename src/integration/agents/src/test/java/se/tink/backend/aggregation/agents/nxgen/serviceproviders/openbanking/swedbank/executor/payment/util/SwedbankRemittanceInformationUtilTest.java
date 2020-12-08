package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.util;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ReferenceType;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SwedbankRemittanceInformationUtilTest {

    private static final String BANK1_SOURCE_ACCOUNT = "52871111111";
    private static final String BANK1_DESTINATION_ACCOUNT = "52031111111";
    private static final String BANK2_DESTINATION_ACCOUNT = "33001111111";
    private static final String MESSAGE_TINK_TEST = "tinkTest";
    private static final String VALID_OCR = "5244245898134";
    private static final String UNSTRUCTURED_10_chars = "1234567890";
    private static final String UNSTRUCTURED_11_chars = "12345678901";
    private static final String UNSTRUCTURED_35_chars = "12345678901234567890123456789012345";
    private static final String UNSTRUCTURED_36_chars = "123456789012345678901234567890123456";

    @Test(expected = ReferenceValidationException.class)
    public void testUnStructuredRemittanceInformationForLongMessage_IntraBank()
            throws PaymentException {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue(UNSTRUCTURED_36_chars);

        SwedbankRemittanceInformationUtil.of(
                createPaymentRequest(
                        Type.SE,
                        remittanceInformation,
                        BANK1_SOURCE_ACCOUNT,
                        BANK1_DESTINATION_ACCOUNT));
    }

    @Test(expected = ReferenceValidationException.class)
    public void testUnStructuredRemittanceInformationForLongMessage_InterBank()
            throws PaymentException {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue(UNSTRUCTURED_11_chars);

        SwedbankRemittanceInformationUtil.of(
                createPaymentRequest(
                        Type.SE,
                        remittanceInformation,
                        BANK1_SOURCE_ACCOUNT,
                        BANK2_DESTINATION_ACCOUNT));
    }

    @Test
    public void testUnStructuredRemittanceInformationForValidMessage_IntraBank()
            throws PaymentException {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue(UNSTRUCTURED_35_chars);

        SwedbankRemittanceInformationUtil remittanceInformationUtil =
                SwedbankRemittanceInformationUtil.of(
                        createPaymentRequest(
                                Type.SE,
                                remittanceInformation,
                                BANK1_SOURCE_ACCOUNT,
                                BANK1_DESTINATION_ACCOUNT));
        Assert.assertEquals(
                remittanceInformation.getValue(),
                remittanceInformationUtil.getRemittanceInformationUnStructured());
        Assert.assertNull(remittanceInformationUtil.getRemittanceInformationStructured());
    }

    @Test
    public void testUnStructuredRemittanceInformationForValidMessage_InterBank()
            throws PaymentException {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue(UNSTRUCTURED_10_chars);

        SwedbankRemittanceInformationUtil remittanceInformationUtil =
                SwedbankRemittanceInformationUtil.of(
                        createPaymentRequest(
                                Type.SE,
                                remittanceInformation,
                                BANK1_SOURCE_ACCOUNT,
                                BANK2_DESTINATION_ACCOUNT));
        Assert.assertEquals(
                remittanceInformation.getValue(),
                remittanceInformationUtil.getRemittanceInformationUnStructured());
        Assert.assertNull(remittanceInformationUtil.getRemittanceInformationStructured());
    }

    @Test
    public void testUnStructuredRemittanceInformationForTypeNull() throws PaymentException {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(null);
        remittanceInformation.setValue(MESSAGE_TINK_TEST);

        SwedbankRemittanceInformationUtil remittanceInformationUtil =
                SwedbankRemittanceInformationUtil.of(
                        createPaymentRequest(
                                Type.SE,
                                remittanceInformation,
                                BANK1_SOURCE_ACCOUNT,
                                BANK2_DESTINATION_ACCOUNT));
        Assert.assertEquals(
                remittanceInformation.getValue(),
                remittanceInformationUtil.getRemittanceInformationUnStructured());
        Assert.assertNotNull(remittanceInformationUtil.getRemittanceInformationUnStructured());
        Assert.assertNull(remittanceInformationUtil.getRemittanceInformationStructured());
    }

    @Test
    public void testStructuredRemittanceInformationForTypeNull_OCR() throws PaymentException {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(null);
        remittanceInformation.setValue(VALID_OCR);

        SwedbankRemittanceInformationUtil remittanceInformationUtil =
                SwedbankRemittanceInformationUtil.of(
                        createPaymentRequest(
                                Type.SE_BG,
                                remittanceInformation,
                                BANK1_SOURCE_ACCOUNT,
                                BANK2_DESTINATION_ACCOUNT));
        Assert.assertEquals(
                remittanceInformation.getValue(),
                remittanceInformationUtil.getRemittanceInformationStructured().getReference());
        Assert.assertEquals(
                RemittanceInformationType.OCR.name(),
                remittanceInformationUtil.getRemittanceInformationStructured().getReferenceType());
        Assert.assertNotNull(remittanceInformationUtil.getRemittanceInformationStructured());
        Assert.assertNull(remittanceInformationUtil.getRemittanceInformationUnStructured());
    }

    @Test
    public void testStructuredRemittanceInformationForTypeNull_MSG() throws PaymentException {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(null);
        remittanceInformation.setValue(MESSAGE_TINK_TEST);

        SwedbankRemittanceInformationUtil remittanceInformationUtil =
                SwedbankRemittanceInformationUtil.of(
                        createPaymentRequest(
                                Type.SE_BG,
                                remittanceInformation,
                                BANK1_SOURCE_ACCOUNT,
                                BANK2_DESTINATION_ACCOUNT));
        Assert.assertEquals(
                remittanceInformation.getValue(),
                remittanceInformationUtil.getRemittanceInformationStructured().getReference());
        Assert.assertEquals(
                ReferenceType.MSG,
                remittanceInformationUtil.getRemittanceInformationStructured().getReferenceType());
        Assert.assertNotNull(remittanceInformationUtil.getRemittanceInformationStructured());
        Assert.assertNull(remittanceInformationUtil.getRemittanceInformationUnStructured());
    }

    @Test(expected = ReferenceValidationException.class)
    public void testUnStructuredRemittanceInformationForEmptyMessage() throws PaymentException {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(null);
        remittanceInformation.setValue("");

        SwedbankRemittanceInformationUtil.of(
                createPaymentRequest(
                        Type.SE,
                        remittanceInformation,
                        BANK1_SOURCE_ACCOUNT,
                        BANK2_DESTINATION_ACCOUNT));
    }

    private PaymentRequest createPaymentRequest(
            Type creditorType,
            RemittanceInformation remittanceInformation,
            String sourceAccount,
            String destinationAccount) {

        Creditor creditor = mock(Creditor.class);

        AccountIdentifier sourceAccountIdentifier =
                AccountIdentifier.create(Type.SE, sourceAccount);

        AccountIdentifier destinationAccountIdentifier =
                AccountIdentifier.create(creditorType, destinationAccount);

        doReturn(creditorType).when(creditor).getAccountIdentifierType();
        doReturn(destinationAccountIdentifier.getIdentifier()).when(creditor).getAccountNumber();
        doReturn(MESSAGE_TINK_TEST).when(creditor).getName();

        doReturn(destinationAccountIdentifier).when(creditor).getAccountIdentifier();

        Debtor debtor = mock(Debtor.class);
        doReturn(Type.SE).when(debtor).getAccountIdentifierType();
        doReturn(sourceAccountIdentifier.getIdentifier()).when(debtor).getAccountNumber();
        doReturn(sourceAccountIdentifier).when(debtor).getAccountIdentifier();

        return new PaymentRequest(
                new Payment.Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1))
                        .withCurrency("SEK")
                        .withRemittanceInformation(remittanceInformation)
                        .build());
    }
}
