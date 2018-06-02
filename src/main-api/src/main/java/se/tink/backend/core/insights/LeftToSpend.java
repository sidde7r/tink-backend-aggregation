package se.tink.backend.core.insights;

import io.protostuff.Tag;
import java.util.List;

public class LeftToSpend implements Insight {
    @Tag(1)
    private String title;
    @Tag(2)
    private String body;
    @Tag(3)
    private List<LeftToSpendByPeriod> mostRecentPeriods;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getBody() {
        return body;
    }

    public List<LeftToSpendByPeriod> getMostRecentPeriods() {
        return mostRecentPeriods;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setMostRecentPeriods(List<LeftToSpendByPeriod> mostRecentPeriods) {
        this.mostRecentPeriods = mostRecentPeriods;
    }
}
