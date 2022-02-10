package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class FabricSupplementalInformationCollectorTest {

    public static final String EXPECTED_OTP = "12345";
    private SupplementalInformationController mockSuppController;

    private FabricSupplementalInformationCollector fabricSupplementalInformationCollector;

    @Before
    public void setup() {
        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        mockSuppController = mock(SupplementalInformationController.class);
        fabricSupplementalInformationCollector =
                new FabricSupplementalInformationCollector(catalog, mockSuppController);
    }

    @Test
    public void shouldThrowExceptionWhenNoOtpComesBack() {
        // given
        when(mockSuppController.askSupplementalInformationSync(any()))
                .thenReturn(Collections.emptyMap());

        // when
        Throwable t = catchThrowable(fabricSupplementalInformationCollector::collectSmsOtp);

        // then
        assertThat(t)
                .isInstanceOf(SupplementalInfoException.class)
                .hasMessage("Supplemental info did not come with otp code!");
    }

    @Test
    public void shouldReturnOtpCodeWhenItIsPresentInSupplementalInfo() {
        // given
        when(mockSuppController.askSupplementalInformationSync(any()))
                .thenReturn(Collections.singletonMap("smsOtpField", EXPECTED_OTP));

        // when
        String resultOtp = fabricSupplementalInformationCollector.collectSmsOtp();

        // then
        assertThat(resultOtp).isEqualTo(EXPECTED_OTP);
    }
}
