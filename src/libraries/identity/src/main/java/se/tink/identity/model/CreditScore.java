package se.tink.libraries.identity.model;

public class CreditScore {
    private String text;
    private int score;
    private int maxScore;

    public CreditScore(String text, int score, int maxScore) {
        this.text = text;
        this.score = score;
        this.maxScore = maxScore;
    }

    public String getText() {
        return text;
    }

    public int getScore() {
        return score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public static CreditScore of(String text, int score, int maxScore) {
        return new CreditScore(text, score, maxScore);
    }
}
