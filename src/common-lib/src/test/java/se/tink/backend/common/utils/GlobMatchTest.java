/**
 * 
 */
package se.tink.backend.common.utils;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.utils.GlobMatch;

public class GlobMatchTest {

    @Test
    public void wildcardAfterTest() {
        String pattern = "AG *";
        String text = "AG Restaurang";
        GlobMatch mather = new GlobMatch();
        Assert.assertTrue("String should match", mather.match(text, pattern));
    }

    @Test
    public void wildcardBeforeTest() {
        String pattern = "* Restaurang";
        String text = "AG Restaurang";
        GlobMatch mather = new GlobMatch();
        Assert.assertTrue("String should match", mather.match(text, pattern));
    }

    @Test
    public void wildcardEmptyTest() {
        String pattern = "7*ELEVEN";
        String text = "7ELEVEN";
        GlobMatch mather = new GlobMatch();
        Assert.assertTrue("String should match", mather.match(text, pattern));
    }

    @Test
    public void wildcardDashTest() {
        String pattern = "7*ELEVEN";
        String text = "7-ELEVEN";
        GlobMatch mather = new GlobMatch();
        Assert.assertTrue("String should match", mather.match(text, pattern));
    }

    @Test
    public void wildcardAfterNegativeTest() {
        String pattern = "PAG *";
        String text = "AG Restaurang";
        GlobMatch mather = new GlobMatch();
        Assert.assertFalse("String should not match", mather.match(text, pattern));
    }

    @Test
    public void wildcardBeforeNegativeTest() {
        String pattern = "* Restauranger";
        String text = "AG Restaurang";
        GlobMatch mather = new GlobMatch();
        Assert.assertFalse("String should not match", mather.match(text, pattern));
    }

    @Test
    public void wildcardMiddleTest() {
        String pattern = "A*ang";
        String text = "AG Restaurang";
        GlobMatch mather = new GlobMatch();
        Assert.assertTrue("String should match", mather.match(text, pattern));
    }

    @Test
    public void doubleMiddleNegativeTest() {
        String pattern = "A*an";
        String text = "AG Restaurang";
        GlobMatch mather = new GlobMatch();
        Assert.assertFalse("String should not match", mather.match(text, pattern));
    }

    @Test
    public void doubleWildcardTest() {
        String pattern = "A*an*";
        String text = "AG Restaurang";
        GlobMatch mather = new GlobMatch();
        Assert.assertTrue("String should match", mather.match(text, pattern));
    }

    @Test
    public void trippleWildcardTest() {
        String pattern = "*GATUK*K*";
        String text = "Challes Gatukök i Stan";
        GlobMatch mather = new GlobMatch();
        Assert.assertTrue("String should match", mather.match(text, pattern));
    }

    @Test
    public void doubleWildcardTest2() {
        String pattern = "*TAXI*";
        String text = "Besta taxin i stan";
        GlobMatch mather = new GlobMatch();
        Assert.assertTrue("String should match", mather.match(text, pattern));
    }

    @Test
    public void wildcardDigitTest() {
        String pattern = "IKEA*";
        String text = "IKEA-KUNGENS KUR";
        GlobMatch mather = new GlobMatch();
        Assert.assertTrue("String should match", mather.match(text, pattern));
    }

    @Test
    public void wildcardMiddleTest2() {
        String pattern = "IZETTLE*HAIRFREE SOLNA SOLNA*";
        String text = "IZETTLE";
        GlobMatch mather = new GlobMatch();
        Assert.assertFalse("String should not match", mather.match(text, pattern));
    }

    @Test
    public void testTree1() {
        List<String> patterns = Lists.newArrayList("LIDL 290 / H*", "LIDL 290 / HAMMA*", "LIDL 290 / HAM*");

        GlobMatchTree<String> tree = new GlobMatchTree<String>(patterns, s -> s);
        
        Assert.assertEquals("LIDL 290 / HAMMA*", tree.match("LIDL 290 / HAMMARBY SJ"));
    }
    
    @Test
    public void testTree() {
        List<String> patterns = Lists.newArrayList("mcdonalds*", "mcdougals*", "ica*", "hemköp*", "*östermalmstorg",
                "*torg", "hund*", "hundan*", "iZettle* Pong*", "torgbonden");

        GlobMatchTree<String> tree = new GlobMatchTree<String>(patterns, s -> s);

        Assert.assertEquals("mcdonalds*", tree.match("mcdonalds hötorget"));
        Assert.assertEquals("mcdonalds*", tree.match("mcdonalds"));
        Assert.assertEquals("mcdougals*", tree.match("mcdougals"));
        Assert.assertEquals("ica*", tree.match("ica hötorget"));
        Assert.assertEquals("hemköp*", tree.match("hemköp östermalmstorg"));
        Assert.assertEquals("*östermalmstorg", tree.match("coop östermalmstorg"));
        Assert.assertEquals("iZettle* Pong*", tree.match("iZettle* Pong Norr"));
        Assert.assertEquals("hund*", tree.match("hundar"));
        Assert.assertEquals(null, tree.match("torgbondens"));
        Assert.assertEquals(null, tree.match("iZettler Pong Norr"));
        Assert.assertEquals(null, tree.match("fredrik"));
        Assert.assertEquals(null, tree.match("mcdonald"));
        Assert.assertEquals(null, tree.match("mcdonal"));
    }
    
    @Test
    public void testTree2() {
        List<String> patterns = Lists.newArrayList("*mq*", "*kiosk", "*biokiosk");

        GlobMatchTree<String> tree = new GlobMatchTree<String>(patterns, s -> s);

        Assert.assertEquals("*biokiosk", tree.match("biokiosk"));
        Assert.assertEquals(null, tree.match("quality grand"));
    }
    
    @Test(timeout=300)
    public void testLotsOfAsterisks() {
        String pattern = "All West Communim391165550bill Paymtpp... More All West Communim391165550bill Paymtppd******* Feller Kord Ref # Less-- /2014";
        String sMatche = "ALL WEST COMMUNIM391165550BILL PAYMTPP... More ALL WEST COMMUNIM391165550BILL PAYMTPPD******* FELLER KORD REF # xxxxxxxxxxx2658 Less--04/18/2014";
        GlobMatch matcher = new GlobMatch();
        Stopwatch stopwatch = Stopwatch.createStarted();
        
        matcher.match(sMatche, pattern);
        
        stopwatch.stop();
        System.out.println(stopwatch);
    }

    /**
     * This is simply a brute force the time complexity of the algorithm. Previously we ran into a bug where time
     * complexity exploded if there were multiple asterisks occuring directly after each other.
     */
    @Test
    public void testLotsOfWildcards() {
        String pattern = " * * * * * * * * * * * * * * * * * * * *";
        String text = "                      ";
        GlobMatch mather = new GlobMatch();
        Assert.assertTrue("String should match", mather.match(text, pattern));
    }
    
    /**
     * This is simply a brute force the time complexity of the algorithm. Previously we ran into a bug where time
     * complexity exploded if there were multiple asterisks occuring directly after each other.
     */
    @Test
    public void testLotsOfWildcards2() {
        String pattern = "* * * * * * * * * * * * * * * * * * * *";
        String text = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        GlobMatch mather = new GlobMatch();
        Assert.assertFalse("String should match", mather.match(text, pattern));
    }
    
    @Test
    public void testAsteriskReduction() {
        Pattern pattern = Pattern.compile("\\*{2,}");
        Assert.assertEquals("*", pattern.matcher("*****").replaceAll("*"));
    }
}
