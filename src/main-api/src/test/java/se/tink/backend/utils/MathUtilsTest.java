package se.tink.backend.utils;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class MathUtilsTest {

    @Test
    public void testWeightedAverage() {

        List<Double> values = Lists.newArrayList(190.0, 184.0, 123.0, 132.0, 112.0, 153.0, 168.0);
        List<Double> weights = Lists.newArrayList(27.0, 24.0, 21.0, 23.0, 22.0, 19.0, 25.0);

        List<ValueWeight> valueWeights = Lists.newArrayList();

        int index = 0;
        for (Double value : values) {
            valueWeights.add(new ValueWeight(value, weights.get(index)));
            index++;
        }

        Double wa = MathUtils.weightedAverage(valueWeights);
        assertEquals(153.640, wa, 0.001);
    }

}
