package se.tink.backend.insights.utils;

import java.util.Comparator;
import se.tink.backend.insights.core.domain.model.Insight;

public class ComparatorUtils {

    public static Comparator<Insight> INSIGHT_BY_SCORE_DESC = Comparator
            .comparingDouble(insight -> insight.getRelevanceScore());
}
