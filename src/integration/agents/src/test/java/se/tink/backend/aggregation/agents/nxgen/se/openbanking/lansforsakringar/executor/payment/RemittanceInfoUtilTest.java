package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import static org.assertj.core.api.Assertions.catchThrowable;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class RemittanceInfoUtilTest {

    @SneakyThrows
    @Test
    public void testValidateAndReturnRemittanceInfo_Success() {

        RemittanceInformation remittanceInformation =
                getRemittanceInformation(RemittanceInformationType.UNSTRUCTURED, "Reference");
        String value = RemittanceInfoUtil.validateAndReturnRemittanceInfo(remittanceInformation);

        Assertions.assertThat(value).isEqualTo("Reference");
    }

    @Test
    public void testValidateAndReturnRemittanceInfo_MessageToLong() {

        RemittanceInformation remittanceInformation =
                getRemittanceInformation(RemittanceInformationType.UNSTRUCTURED, "1234567890123");

        Throwable thrown =
                catchThrowable(
                        () ->
                                RemittanceInfoUtil.validateAndReturnRemittanceInfo(
                                        remittanceInformation));

        Assertions.assertThat(thrown).isInstanceOf(ReferenceValidationException.class);
        Assertions.assertThat(thrown).hasMessage(EndUserMessage.INVALID_MESSAGE.getKey().get());
    }

    @Test
    public void testValidateAndReturnRemittanceInfo_WrongType() {

        RemittanceInformation remittanceInformation =
                getRemittanceInformation(RemittanceInformationType.OCR, "Reference");

        Throwable thrown =
                catchThrowable(
                        () ->
                                RemittanceInfoUtil.validateAndReturnRemittanceInfo(
                                        remittanceInformation));

        Assertions.assertThat(thrown).isInstanceOf(ReferenceValidationException.class);
        Assertions.assertThat(thrown).hasMessage(EndUserMessage.INVALID_MESSAGE.getKey().get());
    }

    @SneakyThrows
    @Test
    public void testValidateRemittanceInfoForGiros_SuccessUnstructured() {

        RemittanceInformation remittanceInformation =
                getRemittanceInformation(RemittanceInformationType.UNSTRUCTURED, "Reference");
        RemittanceInfoUtil.validateRemittanceInfoForGiros(remittanceInformation);
    }

    @SneakyThrows
    @Test
    public void testValidateRemittanceInfoForGiros_SuccessOCR() {

        RemittanceInformation remittanceInformation =
                getRemittanceInformation(RemittanceInformationType.OCR, "Reference");
        RemittanceInfoUtil.validateRemittanceInfoForGiros(remittanceInformation);
    }

    @Test
    public void testValidateRemittanceInfoForGiros_UnstructuredMessageToLong() {

        RemittanceInformation remittanceInformation =
                getRemittanceInformation(
                        RemittanceInformationType.UNSTRUCTURED,
                        "1234567890123123456789012312345678901231234567890123123456789012312345678901231234567890123");

        Throwable thrown =
                catchThrowable(
                        () ->
                                RemittanceInfoUtil.validateRemittanceInfoForGiros(
                                        remittanceInformation));

        Assertions.assertThat(thrown).isInstanceOf(ReferenceValidationException.class);
        Assertions.assertThat(thrown).hasMessage(EndUserMessage.INVALID_MESSAGE.getKey().get());
    }

    @Test
    public void testValidateRemittanceInfoForGiros_OCRMessageToLong() {

        RemittanceInformation remittanceInformation =
                getRemittanceInformation(
                        RemittanceInformationType.OCR,
                        "1234567890123123456789012312345678901231234567890123");

        Throwable thrown =
                catchThrowable(
                        () ->
                                RemittanceInfoUtil.validateRemittanceInfoForGiros(
                                        remittanceInformation));

        Assertions.assertThat(thrown).isInstanceOf(ReferenceValidationException.class);
        Assertions.assertThat(thrown).hasMessage(EndUserMessage.INVALID_OCR.getKey().get());
    }

    @Test
    public void testValidateRemittanceInfoForGiros_WrongType() {

        RemittanceInformation remittanceInformation =
                getRemittanceInformation(RemittanceInformationType.REFERENCE, "Reference");

        Throwable thrown =
                catchThrowable(
                        () ->
                                RemittanceInfoUtil.validateRemittanceInfoForGiros(
                                        remittanceInformation));

        Assertions.assertThat(thrown).isInstanceOf(ReferenceValidationException.class);
        Assertions.assertThat(thrown).hasMessage(EndUserMessage.INVALID_MESSAGE.getKey().get());
    }

    private RemittanceInformation getRemittanceInformation(
            RemittanceInformationType unstructured, String reference) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(unstructured);
        remittanceInformation.setValue(reference);
        return remittanceInformation;
    }
}
