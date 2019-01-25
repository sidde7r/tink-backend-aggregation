package se.tink.backend.aggregation.agents.models.fraud;

import java.util.Objects;

public class FraudCreditScoringContent extends FraudDetailsContent {

    private int score;
    private int maxScore;
    private String text;
    
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public int getMaxScore() {
        return maxScore;
    }
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }
    
    @Override
    public String generateContentId() {
        return String.valueOf(Objects.hash(itemType(), score));
    }
    
    @Override
    public FraudTypes itemType() {
        return FraudTypes.INQUIRY;
    }
}
