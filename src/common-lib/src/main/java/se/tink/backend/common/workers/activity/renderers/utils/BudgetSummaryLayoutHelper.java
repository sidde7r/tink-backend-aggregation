package se.tink.backend.common.workers.activity.renderers.utils;

import se.tink.backend.common.workers.activity.renderers.themes.BudgetSummaryTheme;

public class BudgetSummaryLayoutHelper {

    private BudgetSummaryTheme theme;
    
    public BudgetSummaryLayoutHelper(BudgetSummaryTheme theme) {
        this.theme = theme;
    }

    public int getPieRadius(int availableWidth, int budgetCount) {
        int minimumTotalMargin = (budgetCount + 1) * theme.getMinPieMargin(); 
        int availableWidthExcludingMargins = availableWidth - minimumTotalMargin; 
        int possibleCircleWidth = availableWidthExcludingMargins / budgetCount;
        int pieRadius = Math.min(theme.getMaxPieRadius(), possibleCircleWidth / 2);
        return pieRadius;
    }

    public int getPieMargin(int availableWidth, int budgetCount, int pieRadius) {
        int totalMargin = availableWidth - (pieRadius * 2) * budgetCount;
        int pieMargin = totalMargin / (budgetCount + 1);
        pieMargin = Math.min(theme.getMaxPieMargin(), pieMargin);
        pieMargin = Math.max(theme.getMinPieMargin(), pieMargin);
        return pieMargin;
    }    
}
