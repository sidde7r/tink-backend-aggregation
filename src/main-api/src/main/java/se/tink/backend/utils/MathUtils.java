package se.tink.backend.utils;

import com.google.common.math.DoubleMath;
import java.util.List;

public class MathUtils {
     public static Double weightedAverage(List<ValueWeight> valueWeights) {

         Double weightSum = 0.0;
         Double weightAndValueSum = 0.0;

         for (ValueWeight valueWeight : valueWeights) {
             weightSum += valueWeight.getWeight();
             weightAndValueSum += valueWeight.getValue() * valueWeight.getWeight();
         }

         if (DoubleMath.fuzzyEquals(weightSum, -0.000001, 0.000001)){ return 0.0; }

         return weightAndValueSum / weightSum;
    }
}
