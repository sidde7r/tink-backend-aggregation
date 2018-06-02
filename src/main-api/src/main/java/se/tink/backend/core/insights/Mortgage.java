package se.tink.backend.core.insights;

import io.protostuff.Tag;
import java.util.List;

public class Mortgage implements Insight {
    @Tag(1)
    private String title;
    @Tag(2)
    private String body;
    @Tag(3)
    private List<HistogramBucket> distribution;
    @Tag(4)
    private Double interestRate;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getBody() {
        return body;
    }

    public List<HistogramBucket> getDistribution() {
        return distribution;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setDistribution(List<HistogramBucket> distribution) {
        this.distribution = distribution;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }
}
