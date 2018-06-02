package se.tink.backend.common.merchants;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;

import java.io.IOException;

/**
 * Class generates the same search query as the GaussDeclayFunctionBuilder except that
 * this class formats "origin" as a json object compared to GaussDeclayFunctionBuilder which
 * formats the result as a field.
 */
public class GaussDistanceDecayFunctionBuilder implements ScoreFunctionBuilder {

    private double lat;
    private double lon;
    private String scale;
    private String field;

    @Override
    public String getName() {
        return "gauss";
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(getName());
        builder.startObject(field);
        builder.startObject("origin");
        builder.field("lat", lat);
        builder.field("lon", lon);
        builder.endObject();
        builder.field("scale", scale);
        builder.endObject();
        builder.endObject();
        return builder;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    /**
     * Scale means that documents at the scale will be scored 0.5 (max is 1.0).
     *
     * @param scale
     */
    public void setScale(String scale) {
        this.scale = scale;
    }

    public void setField(String field) {
        this.field = field;
    }
}

