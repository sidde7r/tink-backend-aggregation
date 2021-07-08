package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;

public class ScaMethodFilterTest {

    private static final ScaMethodEntity QR = new ScaMethodEntity("CHIP_TAN", "1", "QR", "asdf");
    private static final ScaMethodEntity OPTICAL =
            new ScaMethodEntity("CHIP_TAN", "1", "OPTICAL", "asdf");
    private static final ScaMethodEntity PUSH_OTP_FIRST =
            new ScaMethodEntity("PUSH_OTP", "1", "Classic - First", "First");
    private static final ScaMethodEntity PUSH_OTP_FIRST_UNEXPECTED =
            new ScaMethodEntity("PUSH_OTP", "1", "Unexpected - First", "First");
    private static final ScaMethodEntity PUSH_DEC_FIRST =
            new ScaMethodEntity("PUSH_DEC", "1", "First", "First");
    private static final ScaMethodEntity PUSH_OTP_SECOND =
            new ScaMethodEntity("PUSH_OTP", "1", "Classic - Second", "Second");
    private static final ScaMethodEntity PUSH_DEC_SECOND =
            new ScaMethodEntity("PUSH_DEC", "1", "Second", "Second");

    private ScaMethodFilter scaMethodFilter = new ScaMethodFilter();

    @Test
    public void shouldRemoveUnsupportedMethods() {
        // given
        List<ScaMethodEntity> input = new ArrayList<>();
        input.add(QR);
        input.add(OPTICAL);
        input.add(PUSH_OTP_FIRST);

        // when
        List<ScaMethodEntity> output = scaMethodFilter.getUsableScaMethods(input);
        // then

        assertThat(output).hasSize(1);
        assertThat(output).containsExactlyInAnyOrder(PUSH_OTP_FIRST);
    }

    @Test
    public void shouldRemovePushTansIfMatchingPushDecFound() {
        // given
        List<ScaMethodEntity> input = new ArrayList<>();
        input.add(PUSH_OTP_FIRST);
        input.add(PUSH_DEC_FIRST);
        input.add(PUSH_OTP_SECOND);
        input.add(PUSH_DEC_SECOND);

        // when
        List<ScaMethodEntity> output = scaMethodFilter.getUsableScaMethods(input);
        // then

        assertThat(output).hasSize(2);
        assertThat(output).containsExactlyInAnyOrder(PUSH_DEC_FIRST, PUSH_DEC_SECOND);
    }

    @Test
    public void shouldNotRemovePushTanIfMoreThanTwoMethodsWithTheSameNameFound() {
        // given
        List<ScaMethodEntity> input = new ArrayList<>();
        input.add(PUSH_OTP_FIRST);
        input.add(PUSH_OTP_FIRST_UNEXPECTED);
        input.add(PUSH_DEC_FIRST);
        input.add(PUSH_OTP_SECOND);
        input.add(PUSH_DEC_SECOND);

        // when
        List<ScaMethodEntity> output = scaMethodFilter.getUsableScaMethods(input);
        // then

        assertThat(output).hasSize(4);
        assertThat(output)
                .containsExactlyInAnyOrder(
                        PUSH_OTP_FIRST, PUSH_OTP_FIRST_UNEXPECTED, PUSH_DEC_FIRST, PUSH_DEC_SECOND);
    }

    @Test
    public void shouldNotRemovePushTanIfNoPushDecFound() {
        // given
        List<ScaMethodEntity> input = new ArrayList<>();
        input.add(PUSH_OTP_FIRST);
        input.add(PUSH_OTP_SECOND);

        // when
        List<ScaMethodEntity> output = scaMethodFilter.getUsableScaMethods(input);
        // then

        assertThat(output).hasSize(2);
        assertThat(output).containsExactlyInAnyOrder(PUSH_OTP_FIRST, PUSH_OTP_SECOND);
    }

    @Test
    public void shouldNotMessWithJustPushDec() {
        // given
        List<ScaMethodEntity> input = new ArrayList<>();
        input.add(PUSH_DEC_FIRST);
        input.add(PUSH_DEC_SECOND);

        // when
        List<ScaMethodEntity> output = scaMethodFilter.getUsableScaMethods(input);
        // then

        assertThat(output).hasSize(2);
        assertThat(output).containsExactlyInAnyOrder(PUSH_DEC_FIRST, PUSH_DEC_SECOND);
    }
}
