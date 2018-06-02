package se.tink.backend.aggregation.agents.utils.jersey;

import org.junit.Test;
import static org.junit.Assert.*;

public class EntityIdentifierTest {

    @Test
    public void testEqualityWithSameData() throws Exception {
        EntityIdentifier obj1 = EntityIdentifier.create("url1", String.class);
        EntityIdentifier obj2 = EntityIdentifier.create("url1", String.class);

        assertTrue(obj1.equals(obj2));
        assertTrue(obj2.equals(obj1));
    }

    @Test
    public void testInequalityWithNullObject() throws Exception {
        EntityIdentifier obj1 = EntityIdentifier.create("url1", String.class);

        assertFalse(obj1.equals(null));
    }

    @Test
    public void testInequalityWithSameUrlButDifferentClass() throws Exception {
        EntityIdentifier obj1 = EntityIdentifier.create("url1", String.class);
        EntityIdentifier obj2 = EntityIdentifier.create("url1", Double.class);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));
    }

    @Test
    public void testInequalityWithSameClassButDifferentUrl() throws Exception {
        EntityIdentifier obj1 = EntityIdentifier.create("url1", String.class);
        EntityIdentifier obj2 = EntityIdentifier.create("url2", String.class);

        assertFalse(obj1.equals(obj2));
        assertFalse(obj2.equals(obj1));
    }

    @Test
    public void testEqualityOfHashWithSameData() throws Exception {
        EntityIdentifier obj1 = EntityIdentifier.create("url1", String.class);
        EntityIdentifier obj2 = EntityIdentifier.create("url1", String.class);

        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    public void testInequalityOfHashWithSameUrlButDifferentClass() throws Exception {
        EntityIdentifier obj1 = EntityIdentifier.create("url1", String.class);
        EntityIdentifier obj2 = EntityIdentifier.create("url1", Double.class);

        assertNotEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    public void testInequalityOfHashWithSameClassButDifferentUrl() throws Exception {
        EntityIdentifier obj1 = EntityIdentifier.create("url1", String.class);
        EntityIdentifier obj2 = EntityIdentifier.create("url2", String.class);

        assertNotEquals(obj1.hashCode(), obj2.hashCode());
    }
}