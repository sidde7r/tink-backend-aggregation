package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import java.util.List;

public class UnusualCategoryActivityData {
    private String categoryId;
    private List<KVPair<String, Double>> data;
    private List<KVPair<String, Double>> dataAverages;
    private String period;

    public String getCategoryId() {
        return categoryId;
    }

    public List<KVPair<String, Double>> getData() {
        return data;
    }

    public List<KVPair<String, Double>> getDataAverages() {
        return dataAverages;
    }

    public String getPeriod() {
        return period;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setData(List<KVPair<String, Double>> data) {
        this.data = data;
    }

    public void setDataAverages(List<KVPair<String, Double>> dataAverages) {
        this.dataAverages = dataAverages;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    @Override
    public String toString() {
    	return MoreObjects.toStringHelper(this).add("categoryId", categoryId).add("data", data).toString();
    }
}