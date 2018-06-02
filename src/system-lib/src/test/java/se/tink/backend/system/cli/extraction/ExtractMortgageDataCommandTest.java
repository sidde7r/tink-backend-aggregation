package se.tink.backend.system.cli.extraction;

import com.google.api.client.util.Lists;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ExtractMortgageDataCommandTest {

    /**
     * Data : [["SBAB 1,5% ränta","2 912 600 kr"]]
     */
    @Test
    public void parseOldLoanDataListForInterestRate() {
        List<List<String>> listList = Lists.newArrayList();
        List<String> l1 = Lists.newArrayList();
        l1.add("SBAB 1,5% ränta");
        l1.add("2 912 600 kr");
        listList.add(l1);

        Assert.assertEquals("1,5", ExtractMortgageDataCommand.getLoanInterestRate(listList));
        Assert.assertEquals(2912600, ExtractMortgageDataCommand.getLoanAmount(listList), 0);
    }

    /**
     * Data : [["SBAB Bolån 3 mån 1,16% ränta","2 912 600 kr"]]
     */
    @Test
    public void parseNewLoanDataListForInterestRate() {
        List<List<String>> listList = Lists.newArrayList();
        List<String> l1 = Lists.newArrayList();
        l1.add("SBAB Bolån 3 mån 1,16% ränta");
        l1.add("2 912 600 kr");
        listList.add(l1);

        Assert.assertEquals("1,16", ExtractMortgageDataCommand.getLoanInterestRate(listList));
        Assert.assertEquals(2912600, ExtractMortgageDataCommand.getLoanAmount(listList), 0);
    }

    /**
     * Data : ["SBAB bolån 1,48 % ränta","2 300 000 kr"]
     */
    @Test
    public void parseNewLoanDataListForInterestRate2() {
        List<List<String>> listList = Lists.newArrayList();
        List<String> l1 = Lists.newArrayList();
        l1.add("SBAB bolån 1,48 % ränta");
        l1.add("2 300 000 kr");
        listList.add(l1);

        Assert.assertEquals("1,48", ExtractMortgageDataCommand.getLoanInterestRate(listList));
        Assert.assertEquals(2300000, ExtractMortgageDataCommand.getLoanAmount(listList), 0);
    }

}
