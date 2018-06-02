package se.tink.backend.system.cli.interestmap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;

public class TinkPolygon extends Polygon {

    private final String areaId;
    private final Polygon polygon;

    public TinkPolygon(String areaId, Polygon polygon) {

        this.areaId = areaId;
        this.polygon = polygon;
    }

    public void setExteriorRing(List<LngLatAlt> points) {
        polygon.setExteriorRing(points);
    }

    @JsonIgnore
    public List<LngLatAlt> getExteriorRing() {
        return polygon.getExteriorRing();
    }

    @JsonIgnore
    public List<List<LngLatAlt>> getInteriorRings() {
        return polygon.getInteriorRings();
    }

    public List<LngLatAlt> getInteriorRing(int index) {
        return polygon.getInteriorRing(index);
    }

    public void addInteriorRing(List<LngLatAlt> points) {
        polygon.addInteriorRing(points);
    }

    public void addInteriorRing(LngLatAlt... points) {
        polygon.addInteriorRing(points);
    }

    public String getAreaId() {
        return areaId;
    }
}

