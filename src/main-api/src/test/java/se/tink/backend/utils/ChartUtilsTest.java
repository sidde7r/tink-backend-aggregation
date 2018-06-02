package se.tink.backend.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ChartUtilsTest {

    @Test
    public void testNumDigits() {
        assertThat(ChartUtils.numDigits(-1)).isEqualTo(1);
        assertThat(ChartUtils.numDigits(0)).isEqualTo(1);
        assertThat(ChartUtils.numDigits(1)).isEqualTo(1);
        assertThat(ChartUtils.numDigits(12)).isEqualTo(2);
        assertThat(ChartUtils.numDigits(123)).isEqualTo(3);
        assertThat(ChartUtils.numDigits(1234)).isEqualTo(4);
        assertThat(ChartUtils.numDigits(12345)).isEqualTo(5);
        assertThat(ChartUtils.numDigits(123456)).isEqualTo(6);
        
        assertThat(ChartUtils.numDigits((int) 1.23)).isEqualTo(1);
    }

    @Test
    public void testResolutionForGuidelines() {
        assertThat(ChartUtils.getResolutionForGuidelines(1)).isEqualTo(0.1);
        assertThat(ChartUtils.getResolutionForGuidelines(2)).isEqualTo(5);
        assertThat(ChartUtils.getResolutionForGuidelines(3)).isEqualTo(10);
        assertThat(ChartUtils.getResolutionForGuidelines(4)).isEqualTo(100);
        assertThat(ChartUtils.getResolutionForGuidelines(5)).isEqualTo(1000);
        assertThat(ChartUtils.getResolutionForGuidelines(6)).isEqualTo(10000);
    }
    
    @Test
    public void testCeiling() {
        assertThat(ChartUtils.ceiling(123, 0)).isEqualTo(123);
        assertThat(ChartUtils.ceiling(123, 0.1)).isEqualTo(123);
        assertThat(ChartUtils.ceiling(123, 1)).isEqualTo(123);
        assertThat(ChartUtils.ceiling(123, 5)).isEqualTo(125);
        assertThat(ChartUtils.ceiling(123, 9)).isEqualTo(126);
        assertThat(ChartUtils.ceiling(123, 10)).isEqualTo(130);
        assertThat(ChartUtils.ceiling(123, 20)).isEqualTo(140);
        assertThat(ChartUtils.ceiling(123, 50)).isEqualTo(150);
        assertThat(ChartUtils.ceiling(123, 100)).isEqualTo(200);
        assertThat(ChartUtils.ceiling(123, 500)).isEqualTo(500);
        
        assertThat(ChartUtils.ceiling(1.23, 0)).isEqualTo(1.23);
        assertThat(ChartUtils.ceiling(1.23, 0.5)).isEqualTo(1.5);
        assertThat(ChartUtils.ceiling(1.23, 1)).isEqualTo(2);
        assertThat(ChartUtils.ceiling(1.23, 1.5)).isEqualTo(1.5);
        assertThat(ChartUtils.ceiling(1.23, 2)).isEqualTo(2);
        
        assertThat(ChartUtils.ceiling(0.123, 0)).isEqualTo(0.123);
        assertThat(ChartUtils.ceiling(0.123, 0.01)).isEqualTo(0.13);
        assertThat(ChartUtils.ceiling(0.123, 0.1)).isEqualTo(0.2);
    }
    
    @Test
    public void testFloor() {
        assertThat(ChartUtils.floor(123, 0)).isEqualTo(123);
        assertThat(ChartUtils.floor(123, 0.1)).isEqualTo(123);
        assertThat(ChartUtils.floor(123, 1)).isEqualTo(123);
        assertThat(ChartUtils.floor(123, 5)).isEqualTo(120);
        assertThat(ChartUtils.floor(123, 9)).isEqualTo(117);
        assertThat(ChartUtils.floor(123, 10)).isEqualTo(120);
        assertThat(ChartUtils.floor(123, 20)).isEqualTo(120);
        assertThat(ChartUtils.floor(123, 50)).isEqualTo(100);
        assertThat(ChartUtils.floor(123, 100)).isEqualTo(100);
        assertThat(ChartUtils.floor(123, 500)).isEqualTo(0);
        
        assertThat(ChartUtils.floor(1.23, 0)).isEqualTo(1.23);
        assertThat(ChartUtils.floor(1.23, 0.5)).isEqualTo(1.0);
        assertThat(ChartUtils.floor(1.23, 1)).isEqualTo(1.0);
        assertThat(ChartUtils.floor(1.23, 1.5)).isEqualTo(0);
        assertThat(ChartUtils.floor(1.23, 2)).isEqualTo(0);
        
        assertThat(ChartUtils.floor(0.123, 0)).isEqualTo(0.123);
        assertThat(ChartUtils.floor(0.123, 0.01)).isEqualTo(0.12);
        assertThat(ChartUtils.floor(0.123, 0.1)).isEqualTo(0.1);
    }
    
    @Test
    public void testGuidelines() {

        assertThat(ChartUtils.getGuidelines(0, 10000, 2)).containsExactly(10000d, 5000d);
        assertThat(ChartUtils.getGuidelines(0, 10000, 3)).containsExactly(8000d, 4000d);
        assertThat(ChartUtils.getGuidelines(0, 10000, 4)).containsExactly(9000d, 6000d, 3000d);
        
        assertThat(ChartUtils.getGuidelines(0, 10001, 2)).containsExactly(6000d);
        assertThat(ChartUtils.getGuidelines(0, 10001, 3)).containsExactly(8000d, 4000d);
        assertThat(ChartUtils.getGuidelines(0, 10001, 4)).containsExactly(9000d, 6000d, 3000d);
        
        assertThat(ChartUtils.getGuidelines(-6000, 6000, 2)).containsExactly(6000d, -6000d);
        assertThat(ChartUtils.getGuidelines(-6000, 6000, 3)).containsExactly(4000d, -4000d);
        assertThat(ChartUtils.getGuidelines(-6000, 6000, 4)).containsExactly(6000d, 3000d, -3000d, -6000d);
        assertThat(ChartUtils.getGuidelines(-6000, 6000, 5)).containsExactly(6000d, 3000d, -3000d, -6000d);
        assertThat(ChartUtils.getGuidelines(-6000, 6000, 6)).containsExactly(6000d, 4000d, 2000d, -2000d, -4000d, -6000d);
        assertThat(ChartUtils.getGuidelines(-6000, 6000, 7)).containsExactly(6000d, 4000d, 2000d, -2000d, -4000d, -6000d);
        
        assertThat(ChartUtils.getGuidelines(-6000, 9000, 2)).containsExactly(8000d);
        assertThat(ChartUtils.getGuidelines(-6000, 9000, 3)).containsExactly(5000d, -5000d);
        assertThat(ChartUtils.getGuidelines(-6000, 9000, 4)).containsExactly(8000d, 4000d, -4000d);
        assertThat(ChartUtils.getGuidelines(-6000, 9000, 5)).containsExactly(9000d, 6000d, 3000d, -3000d, -6000d);
        
        assertThat(ChartUtils.getGuidelines(0, 97, 3)).containsExactly(70d, 35d);
        assertThat(ChartUtils.getGuidelines(0, 97, 4)).containsExactly(75d, 50d, 25d);
          
        assertThat(ChartUtils.getGuidelines(0, 0, 2)).isEmpty();
        assertThat(ChartUtils.getGuidelines(10, -10, 2)).isEmpty();
        
        assertThat(ChartUtils.getGuidelines(0, 5, 2)).containsExactly(5d, 2.5d);
        
        assertThat(ChartUtils.getGuidelines(-1, 1, 2)).containsExactly(1d, -1d);
        assertThat(ChartUtils.getGuidelines(-1, 1, 3)).containsExactly(0.7, -0.7);
        assertThat(ChartUtils.getGuidelines(-1, 1, 4)).containsExactly(1d, 0.5, -0.5, -1d);
        assertThat(ChartUtils.getGuidelines(-1, 1, 5)).containsExactly(0.8, 0.4, -0.4, -0.8);
        assertThat(ChartUtils.getGuidelines(-1, 1, 6)).containsExactly(0.8, 0.4, -0.4, -0.8);
    }
}
