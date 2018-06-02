package se.tink.backend.common.workers.activity.generators.models;

import com.google.common.base.MoreObjects;
import java.util.List;
import se.tink.backend.core.KVPair;

public class NetIncomeActivityData {
    private List<KVPair<String, Double>> data;

    public NetIncomeActivityData() {

    }

    public List<KVPair<String, Double>> getData() {
        return data;
    }

    public void setData(List<KVPair<String, Double>> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("data", data).toString();
    }
}
