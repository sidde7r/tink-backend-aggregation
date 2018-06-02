package se.tink.backend.common.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import se.tink.backend.common.utils.EsSearchUtils;

import com.google.common.collect.Sets;

public class EsSearchUtilsTest {

    @Test
    public void testModifiedCities() {
        
        List<String> cities = new ArrayList<String>();
        
        cities = addModifiedCities("å");
        Assert.assertTrue(Sets.newHashSet(cities).contains("a"));
        cities.clear();
        
        cities = addModifiedCities("ä");
        Assert.assertTrue(Sets.newHashSet(cities).contains("a"));
        Assert.assertTrue(Sets.newHashSet(cities).contains("e"));
        cities.clear();
        
        cities = addModifiedCities("ö");
        Assert.assertTrue(Sets.newHashSet(cities).contains("o"));
        cities.clear();
        
        cities = addModifiedCities("åä");
        Assert.assertTrue(Sets.newHashSet(cities).contains("aa"));
        Assert.assertTrue(Sets.newHashSet(cities).contains("ae"));
        cities.clear();
        
        cities = addModifiedCities("äö");
        Assert.assertTrue(Sets.newHashSet(cities).contains("ao"));
        Assert.assertTrue(Sets.newHashSet(cities).contains("eo"));
        cities.clear();
        
        cities = addModifiedCities("åö");
        Assert.assertTrue(Sets.newHashSet(cities).contains("ao"));
        cities.clear();
        
        cities = addModifiedCities("åäö");
        Assert.assertTrue(Sets.newHashSet(cities).contains("aao"));
        Assert.assertTrue(Sets.newHashSet(cities).contains("aeo"));
        cities.clear();
    }
    
    private List<String> addModifiedCities(String city) {
        return EsSearchUtils.addModifiedCities(city, 4);
    }
}
