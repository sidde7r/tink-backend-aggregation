package se.tink.backend.system.cli.interestmap;

import com.google.common.collect.Lists;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PolygonsTest {

    private Polygon simplePolygon;
    private Polygon concavePolygon;
    private Polygon polygonWithHole;

    private static LngLatAlt c(double lat, double lng) {
        return new LngLatAlt(lng, lat);
    }

    @Before
    public void setUp() {
        simplePolygon = new Polygon();
        simplePolygon.setExteriorRing(Lists.newArrayList(c(10, 10), c(10, 20), c(20, 20), c(20, 10)));

        concavePolygon = new Polygon();
        concavePolygon.setExteriorRing(Lists.newArrayList(c(10, 10), c(10, 20), c(15, 11), c(20, 20), c(20, 10)));

        polygonWithHole = new Polygon();
        polygonWithHole.setExteriorRing(Lists.newArrayList(c(10, 10), c(10, 20), c(20, 10)));
        polygonWithHole.addInteriorRing(Lists.newArrayList(c(12, 12), c(12, 15), c(15, 12)));
    }

    @Test
    public void testCoordsInsideSimplePolygon() {

        Assert.assertTrue(Polygons.isCoordinatesInside(simplePolygon, 15, 15));
        Assert.assertTrue(Polygons.isCoordinatesInside(simplePolygon, 15, 16));
        Assert.assertTrue(Polygons.isCoordinatesInside(simplePolygon, 19, 10.1));
        Assert.assertTrue(Polygons.isCoordinatesInside(simplePolygon, 10.01, 10.01));
        Assert.assertTrue(Polygons.isCoordinatesInside(simplePolygon, 10.00001, 10.000001));

        Assert.assertFalse(Polygons.isCoordinatesInside(simplePolygon, -15, 15));
        Assert.assertFalse(Polygons.isCoordinatesInside(simplePolygon, 8, 16));
        Assert.assertFalse(Polygons.isCoordinatesInside(simplePolygon, 25, 25));
        Assert.assertFalse(Polygons.isCoordinatesInside(simplePolygon, 10, 9.9));
        Assert.assertFalse(Polygons.isCoordinatesInside(simplePolygon, 9.99999, 9.99999));
    }

    @Test
    public void testCoordsofPolygonWithHole() {
        Assert.assertTrue(Polygons.isCoordinatesInside(polygonWithHole, 11, 17));
        Assert.assertTrue(Polygons.isCoordinatesInside(polygonWithHole, 16, 11));
        Assert.assertTrue(Polygons.isCoordinatesInside(polygonWithHole, 10.01, 10.01));

        Assert.assertFalse(Polygons.isCoordinatesInside(polygonWithHole, 12.1, 12.1)); //inside hole
        Assert.assertFalse(Polygons.isCoordinatesInside(polygonWithHole, 8, 16)); //outside everything
        Assert.assertFalse(Polygons.isCoordinatesInside(polygonWithHole, 25, 25)); // outside everything
        Assert.assertFalse(Polygons.isCoordinatesInside(polygonWithHole, 13, 13)); //inside hole
    }

    @Test
    public void testCoordsInsideConcavePolygon() {
        Assert.assertTrue(Polygons.isCoordinatesInside(concavePolygon, 11, 17));
        Assert.assertTrue(Polygons.isCoordinatesInside(concavePolygon, 16, 11));
        Assert.assertTrue(Polygons.isCoordinatesInside(concavePolygon, 10.01, 10.01));
        Assert.assertTrue(Polygons.isCoordinatesInside(concavePolygon, 12.1, 12.1));

        Assert.assertFalse(Polygons.isCoordinatesInside(concavePolygon, 15, 15));
        Assert.assertFalse(Polygons.isCoordinatesInside(concavePolygon, 13, 18));
        Assert.assertFalse(Polygons.isCoordinatesInside(concavePolygon, 25, 25));
        Assert.assertFalse(Polygons.isCoordinatesInside(concavePolygon, 15, 11.1));
    }
}