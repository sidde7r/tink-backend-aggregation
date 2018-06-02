package se.tink.backend.common.mail.monthly.summary.model;

import se.tink.backend.core.Category;

public class CategoryData {

    private Category category;
    private double currentPeriodShare;
    private double previousPeriodShare;

    public CategoryData(){

    }

    public CategoryData(Category category, double currentPeriodShare, double previousPeriodShare) {
        this.category = category;
        this.currentPeriodShare = currentPeriodShare;
        this.previousPeriodShare = previousPeriodShare;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getCurrentPeriodShare() {
        return currentPeriodShare;
    }

    public void setCurrentPeriodShare(double currentPeriodShare) {
        this.currentPeriodShare = currentPeriodShare;
    }

    public void setPreviousPeriodShare(double previousPeriodShare) {
        this.previousPeriodShare = previousPeriodShare;
    }

    public boolean isEmpty() {
        return getCurrentPeriodShareInPercent() == 0 && getPreviousPeriodShareInPercent() == 0;
    }

    public int getCurrentPeriodShareInPercent() {
        return (int) Math.round(currentPeriodShare * 100);
    }

    public int getPreviousPeriodShareInPercent() {
        return (int) Math.round(previousPeriodShare * 100);
    }

    public int getDifferenceInPercentUnits() {
        return getCurrentPeriodShareInPercent() - getPreviousPeriodShareInPercent();
    }

    public int getAbsoluteDifferenceInPercentUnits(){
        return Math.abs(getDifferenceInPercentUnits());
    }

}
