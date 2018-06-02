package se.tink.backend.grpc.v1.converter.statistic;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.core.insights.Categories;
import se.tink.backend.core.insights.DailySpending;
import se.tink.backend.core.insights.Insight;
import se.tink.backend.core.insights.InsightsResponse;
import se.tink.backend.core.insights.LeftToSpend;
import se.tink.backend.core.insights.Mortgage;
import se.tink.backend.core.insights.Savings;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.models.InsightsCategories;
import se.tink.grpc.v1.models.InsightsDailySpend;
import se.tink.grpc.v1.models.InsightsLeftToSpend;
import se.tink.grpc.v1.models.InsightsMortgage;
import se.tink.grpc.v1.models.InsightsSavings;

public class InsightsResponseConverter implements Converter<InsightsResponse, se.tink.grpc.v1.rpc.InsightsResponse> {

    private String currencyCode;

    public InsightsResponseConverter(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public se.tink.grpc.v1.rpc.InsightsResponse convertFrom(InsightsResponse input) {
        se.tink.grpc.v1.rpc.InsightsResponse.Builder builder = se.tink.grpc.v1.rpc.InsightsResponse.newBuilder();

        Categories categories = input.getCategories();
        if (categories != null) {

            List<InsightsCategories.AmountByCategoryCode> list = categories.getHighestExpensesLastPeriod().stream()
                    .map(x -> InsightsCategories.AmountByCategoryCode.newBuilder()
                            .setAmount(NumberUtils.toCurrencyDenominatedAmount(x.getAmount(), currencyCode))
                            .setCategoryCode(x.getCategoryCode()).build()).collect(Collectors.toList());
            builder.setCategories(
                    InsightsCategories.newBuilder()
                            .setTitle(categories.getTitle())
                            .setBody(categories.getBody())
                            .addAllAmountByCategoryCode(list)
                            .build());
        }

        Mortgage mortgage = input.getMortgage();
        if (mortgage != null) {

            List<InsightsMortgage.HistogramBucket> list = mortgage.getDistribution().stream()
                    .map(x -> InsightsMortgage.HistogramBucket.newBuilder()
                            .setLowerEndpoint(x.getLowerEndpoint().doubleValue())
                            .setUpperEndpoint(x.getUpperEndpoint().doubleValue())
                            .setFrequency(x.getFrequency().doubleValue()).build()).collect(Collectors.toList());

            builder.setMortgage(InsightsMortgage.newBuilder()
                    .setTitle(mortgage.getTitle())
                    .setBody(mortgage.getBody())
                    .setInterestRate(mortgage.getInterestRate())
                    .addAllDistribution(list)
                    .build());
        }

        Savings savings = input.getSavings();
        if (savings != null) {
            builder.setSavings(InsightsSavings.newBuilder()
                    .setTitle(savings.getTitle())
                    .setBody(savings.getBody())
                    .setAmount(NumberUtils.toCurrencyDenominatedAmount(savings.getAmount(), currencyCode))
                    .build());
        }

        DailySpending dailySpending = input.getDailySpending();
        if (dailySpending != null) {
            List<InsightsDailySpend.AmountByWeekday> list = dailySpending.getWeekdays().stream()
                    .map(x -> InsightsDailySpend.AmountByWeekday.newBuilder()
                            .setAmount(NumberUtils.toCurrencyDenominatedAmount(x.getAmount(), currencyCode))
                            .setWeekday(x.getWeekday()).build()).collect(Collectors.toList());

            builder.setDailySpend(
                    InsightsDailySpend.newBuilder()
                            .setTitle(dailySpending.getTitle())
                            .setBody(dailySpending.getBody())
                            .addAllAmountByWeekday(list)
                            .build());
        }

        LeftToSpend leftToSpend = input.getLeftToSpend();
        if (leftToSpend != null) {

            List<InsightsLeftToSpend.LeftToSpendByPeriod> list = leftToSpend.getMostRecentPeriods().stream()
                    .map(x -> InsightsLeftToSpend.LeftToSpendByPeriod.newBuilder().setPercentage(x.getPercentage())
                            .setPeriod(x.getPeriod()).build()).collect(Collectors.toList());

            builder.setLeftToSpend(
                    InsightsLeftToSpend.newBuilder()
                            .setTitle(leftToSpend.getTitle())
                            .setBody(leftToSpend.getBody())
                            .addAllLeftToSpendByPeriod(list)
                            .build());
        }

        return builder.build();
    }
}
