package se.tink.backend.common.providers.booli.entities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaData {
    private Double biddingAveragePrediction;
    private Double biddingAverageWeight;
    private Double differenceAverage;
    private Double differenceCv;
    private Double knnPrediction;
    private Double knnWeight;
    private String predictionDate;
    private String predictor;
    private Double previousSalePrediction;
    private Double previousSaleWeight;
    private Double priceCv;
    private String recommendation;

    public Double getBiddingAveragePrediction() {
        return biddingAveragePrediction;
    }

    public void setBiddingAveragePrediction(Double biddingAveragePrediction) {
        this.biddingAveragePrediction = biddingAveragePrediction;
    }

    public Double getBiddingAverageWeight() {
        return biddingAverageWeight;
    }

    public void setBiddingAverageWeight(Double biddingAverageWeight) {
        this.biddingAverageWeight = biddingAverageWeight;
    }

    public Double getDifferenceAverage() {
        return differenceAverage;
    }

    public void setDifferenceAverage(Double differenceAverage) {
        this.differenceAverage = differenceAverage;
    }

    public Double getDifferenceCv() {
        return differenceCv;
    }

    public void setDifferenceCv(Double differenceCv) {
        this.differenceCv = differenceCv;
    }

    public Double getKnnPrediction() {
        return knnPrediction;
    }

    public void setKnnPrediction(Double knnPrediction) {
        this.knnPrediction = knnPrediction;
    }

    public Double getKnnWeight() {
        return knnWeight;
    }

    public void setKnnWeight(Double knnWeight) {
        this.knnWeight = knnWeight;
    }

    public String getPredictionDate() {
        return predictionDate;
    }

    public void setPredictionDate(String predictionDate) {
        this.predictionDate = predictionDate;
    }

    public String getPredictor() {
        return predictor;
    }

    public void setPredictor(String predictor) {
        this.predictor = predictor;
    }

    public Double getPreviousSalePrediction() {
        return previousSalePrediction;
    }

    public void setPreviousSalePrediction(Double previousSalePrediction) {
        this.previousSalePrediction = previousSalePrediction;
    }

    public Double getPreviousSaleWeight() {
        return previousSaleWeight;
    }

    public void setPreviousSaleWeight(Double previousSaleWeight) {
        this.previousSaleWeight = previousSaleWeight;
    }

    public Double getPriceCv() {
        return priceCv;
    }

    public void setPriceCv(Double priceCv) {
        this.priceCv = priceCv;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("biddingAveragePrediction", biddingAveragePrediction)
                .add("biddingAverageWeight", biddingAverageWeight)
                .add("differenceAverage", differenceAverage)
                .add("differenceCv", differenceCv)
                .add("knnPrediction", knnPrediction)
                .add("knnWeight", knnWeight)
                .add("predictionDate", predictionDate)
                .add("predictor", predictor)
                .add("previousSalePrediction", previousSalePrediction)
                .add("previousSaleWeight", previousSaleWeight)
                .add("priceCv", priceCv)
                .add("recommendation", recommendation)
                .toString();
    }
}
