package se.tink.backend.main.validators;

import javax.ws.rs.WebApplicationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.config.TransfersConfiguration;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.main.validators.exception.AbstractTransferException;
import se.tink.backend.main.validators.exception.InstantiationException;
import se.tink.backend.main.validators.exception.TransferValidationException;
import static org.mockito.Mockito.mock;
import static se.tink.backend.main.validators.exception.AbstractTransferException.LogMessage;
import static se.tink.backend.main.validators.exception.AbstractTransferException.LogMessageParametrized;

public class TransferUpdateRequestValidatorTest extends AbstractValidatorTest {
    private TransferUpdateRequestValidator validator;

    @Before
    public void setup() throws InstantiationException {
        TransfersConfiguration transferConfiguration = mock(TransfersConfiguration.class);
        transfer = createPayment(BARNCANCERFONDEN_BG);

        validator = new TransferUpdateRequestValidator(transferConfiguration);
    }

    @Test
    public void ensureValidateUpdates_throwsBadRequest_whenIncomingTransfer_isNull() throws TransferValidationException {
        try {
            validator.validateUpdates(null, transfer);
        } catch (WebApplicationException e) {
            Assert.assertEquals(e.getResponse().getStatus(), 400);
        }
    }

    @Test
    public void ensureValidateUpdates_throwsException_whenExistingTransfer_isNull() throws TransferValidationException {
        expect(LogMessage.MISSING_EXISTING_TRANSFER);

        validator.validateUpdates(transfer, null);
    }

    @Test
    public void ensureValidateDestinationUpdate_throwsException_whenPaymentDestination_isUpdated() throws TransferValidationException {
        Transfer existingPayment = createPayment(PG_IDENTIFIER);

        expect(LogMessageParametrized.UPDATED_DESTINATION,
                existingPayment.getDestination());

        validator.validateUpdates(transfer, existingPayment);
    }

    @Test
    public void ensureValidateDestinationUpdate_throwsException_whenEInvoiceDestination_isUpdated() throws TransferValidationException {
        transfer.setType(TransferType.EINVOICE);

        Transfer existingEInvoice = createEInvoice(PG_IDENTIFIER);

        expect(LogMessageParametrized.UPDATED_DESTINATION,
                existingEInvoice.getDestination());

        validator.validateUpdates(transfer, existingEInvoice);
    }

    @Test
    public void ensureValidateDestinationUpdate_throwsException_whenPaymentDestinationType_isUpdated() throws TransferValidationException {
        Transfer existingPayment = createPayment(BARNCANCERFONDEN_PG);

        expect(LogMessageParametrized.UPDATED_DESTINATION,
                existingPayment.getDestination());

        validator.validateUpdates(transfer, existingPayment);
    }

    @Test
    public void ensureValidateDestinationUpdate_throwsException_whenEInvoiceDestinationType_isUpdated() throws TransferValidationException {
        transfer.setType(TransferType.EINVOICE);

        Transfer existingEInvoice = createEInvoice(BARNCANCERFONDEN_PG);

        expect(LogMessageParametrized.UPDATED_DESTINATION,
                existingEInvoice.getDestination());

        validator.validateUpdates(transfer, existingEInvoice);
    }

    @Test
    public void ensureValidateDestinationUpdate_validationPasses_whenPaymentDestination_isNotUpdated() throws TransferValidationException {
        Transfer existingPayment = createPayment(BARNCANCERFONDEN_BG);

        validator.validateUpdates(transfer, existingPayment);
    }

    @Test
    public void ensureValidateDestinationUpdate_validationPasses_whenEInvoiceDestination_isNotUpdated() throws TransferValidationException {
        transfer.setType(TransferType.EINVOICE);

        Transfer existingEInvoice = createEInvoice(BARNCANCERFONDEN_BG);

        validator.validateUpdates(transfer, existingEInvoice);
    }

    @Test
    public void ensureValidateUpdatedTransferType_throwsException_whenTransferType_isUpdated() throws TransferValidationException {
        Transfer existingEInvoice = createEInvoice(BARNCANCERFONDEN_BG);

        expect(LogMessage.UPDATED_TRANSFER_TYPE);

        validator.validateUpdates(transfer, existingEInvoice);
    }

    @Test
    public void ensureValidateUpdatedTransferType_validationPasses_whenTransferType_isNotUpdated() throws TransferValidationException {
        Transfer existingPayment = createPayment(BARNCANCERFONDEN_BG);

        validator.validateUpdates(transfer, existingPayment);
    }

    @Test
    public void ensureValidateUpdates_throwsException_whenExistingTransferType_isBankTransfer_andIncomingTransferType_isPayment() throws TransferValidationException {
        Transfer existingTransfer = createTransfer(NORDEA_SSN_IDENTIFIER);

        expect(LogMessage.UPDATED_TRANSFER_TYPE);

        validator.validateUpdates(transfer, existingTransfer);
    }

    @Test
    public void ensureValidateUpdates_throwsException_whenExistingTransferType_isBankTransfer_andIncomingTransferType_isEInvoice() throws TransferValidationException {
        transfer.setType(TransferType.EINVOICE);
        Transfer existingTransfer = createTransfer(NORDEA_SSN_IDENTIFIER);

        expect(LogMessage.UPDATED_TRANSFER_TYPE);

        validator.validateUpdates(transfer, existingTransfer);
    }

    @Test
    public void ensureValidateUpdates_throwsException_whenExistingTransferType_isPayment_andIncomingTransferType_isEInvoice() throws TransferValidationException {
        transfer.setType(TransferType.EINVOICE);
        Transfer existingPayment = createPayment(BARNCANCERFONDEN_BG);

        expect(LogMessage.UPDATED_TRANSFER_TYPE);

        validator.validateUpdates(transfer, existingPayment);
    }

    @Test
    public void ensureValidateUpdates_throwsException_whenExistingTransferType_isPayment_andIncomingTransferType_isBankTransfer() throws TransferValidationException {
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDestination(NORDEA_SSN_IDENTIFIER);
        Transfer existingPayment = createPayment(NORDEA_SSN_IDENTIFIER);

        expect(LogMessage.UPDATED_TRANSFER_TYPE);

        validator.validateUpdates(transfer, existingPayment);
    }

    @Test
    public void ensureValidateUpdates_throwsException_whenExistingTransferType_isEInvoice_andIncomingTransferType_isBankTransfer() throws TransferValidationException {
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDestination(NORDEA_SSN_IDENTIFIER);
        Transfer existingEinvoice = createEInvoice(NORDEA_SSN_IDENTIFIER);

        expect(LogMessage.UPDATED_TRANSFER_TYPE);

        validator.validateUpdates(transfer, existingEinvoice);
    }

    @Test
    public void ensureValidateUpdates_throwsException_whenExistingTransferType_isEInvoice_andIncomingTransferType_isPayment() throws TransferValidationException {
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDestination(NORDEA_SSN_IDENTIFIER);
        Transfer existingPayment = createPayment(NORDEA_SSN_IDENTIFIER);

        expect(LogMessage.UPDATED_TRANSFER_TYPE);

        validator.validateUpdates(transfer, existingPayment);
    }

    @Test
    public void ensureValidateUpdates_throwsException_whenPaymentDestination_isUpdated() throws TransferValidationException {
        Transfer existingPayment = createPayment(PG_IDENTIFIER);

        expect(LogMessageParametrized.UPDATED_DESTINATION,
                existingPayment.getDestination());

        validator.validateUpdates(transfer, existingPayment);
    }

    @Test
    public void ensureValidateUpdates_throwsException_whenEInvoiceDestination_isUpdated() throws TransferValidationException {
        Transfer existingEInvoice = createEInvoice(PG_IDENTIFIER);

        expect(LogMessageParametrized.UPDATED_DESTINATION,
                existingEInvoice.getDestination());

        validator.validateUpdates(transfer, existingEInvoice);
    }

    @Test
    public void ensureValidateUpdates_validationPasses_whenPaymentDestination_isNotUpdated() throws TransferValidationException {
        Transfer existingPayment = createPayment(BARNCANCERFONDEN_BG);

        validator.validateUpdates(transfer, existingPayment);
    }

    @Test
    public void ensureValidateUpdates_validationPasses_whenEInvoiceDestination_isNotUpdated() throws TransferValidationException {
        transfer.setType(TransferType.EINVOICE);
        Transfer existingEInvoice = createEInvoice(BARNCANCERFONDEN_BG);

        validator.validateUpdates(transfer, existingEInvoice);
    }

    @Test
    public void ensureValidateUpdates_validationPasses_whenBankTransfer_isUpdated() throws TransferValidationException {
        transfer.setType(TransferType.BANK_TRANSFER);

        Transfer existingTransfer = createTransfer(NORDEA_SSN_IDENTIFIER);
        existingTransfer.setDestination(SHB_IDENTIFIER);

        validator.validateUpdates(transfer, existingTransfer);
    }

    public String getExpectedMessage(AbstractTransferException.LogMessage message) {
        return String.format("Transfer validation failed ( %s )", message.get());
    }
}
