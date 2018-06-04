package se.tink.backend.core;

public class ProviderTries {
    private Integer onetryfailure;
    private String providername;
    private Integer threetriesfailure;
    private Integer tries;
    private Integer twotriesfailure;

    public Integer getOnetryfailure() {
        return onetryfailure;
    }

    public String getProvidername() {
        return providername;
    }

    public Integer getThreetriesfailure() {
        return threetriesfailure;
    }

    public Integer getTries() {
        return tries;
    }

    public Integer getTwotriesfailure() {
        return twotriesfailure;
    }

    public void setOnetryfailure(Integer onetryfailure) {
        this.onetryfailure = onetryfailure;
    }

    public void setProvidername(String providername) {
        this.providername = providername;
    }

    public void setThreetriesfailure(Integer threetriesfailure) {
        this.threetriesfailure = threetriesfailure;
    }

    public void setTries(Integer tries) {
        this.tries = tries;
    }

    public void setTwotriesfailure(Integer twotriesfailure) {
        this.twotriesfailure = twotriesfailure;
    }
}
