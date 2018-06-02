package se.tink.libraries.math;

import java.math.BigDecimal;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class MathUtilsTest {

    @Test
    public void ceiling() {

        BigDecimal resolution = BigDecimal.valueOf(5,2); // 0.05

        BigDecimal value1 = BigDecimal.valueOf(17,2); // 0.17
        assertThat(MathUtils.ceiling(value1, resolution).doubleValue()).isEqualTo(0.2);

        BigDecimal value2 = BigDecimal.valueOf(12,2); // 0.12
        assertThat(MathUtils.ceiling(value2, resolution).doubleValue()).isEqualTo(0.15);
    }

    @Test
    public void floor() {

        BigDecimal resolution = BigDecimal.valueOf(5,2); // 0.05

        BigDecimal value1 = BigDecimal.valueOf(17,2); // 0.17
        assertThat(MathUtils.floor(value1, resolution).doubleValue()).isEqualTo(0.15);

        BigDecimal value2 = BigDecimal.valueOf(12,2); // 0.12
        assertThat(MathUtils.floor(value2, resolution).doubleValue()).isEqualTo(0.10);

        BigDecimal value3 = BigDecimal.valueOf(4,2); // 0.04
        assertThat(MathUtils.floor(value3, resolution).doubleValue()).isEqualTo(0);
    }
}
