package se.tink.backend.system.document;

import com.fasterxml.jackson.core.type.TypeReference;
import java.awt.Point;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.system.document.file.svg.SignatureSVG;
import se.tink.backend.system.document.mapper.GenericApplicationToDocumentUserMapper;
import se.tink.libraries.metrics.MetricCollector;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class SignatureTest {

    private TypeReference<List<List<Point>>> LIST_OF_LIST_OF_POINTS = new TypeReference<List<List<Point>>>() {};;
    private String signature;
    private String signatureWithConsecutiveDuplicates;

    @Before
    public void setUp() {
        signature = "[ [ { \"x\": 448, \"y\": 212 }, { \"x\": 451, \"y\": 245 }, { \"x\": 454, \"y\": 267 }, { \"x\": 455, \"y\": 268 }, { \"x\": 459, \"y\": 247 }, { \"x\": 468, \"y\": 164 }, { \"x\": 472, \"y\": 141 }, { \"x\": 473, \"y\": 148 }, { \"x\": 480, \"y\": 192 }, { \"x\": 485, \"y\": 197 }, { \"x\": 489, \"y\": 194 } ], [ { \"x\": 534, \"y\": 207 } ], [ { \"x\": 790, \"y\": 270 }, { \"x\": 802, \"y\": 256 }, { \"x\": 815, \"y\": 198 }, { \"x\": 815, \"y\": 169 }, { \"x\": 813, \"y\": 159 }, { \"x\": 812, \"y\": 159 }, { \"x\": 810, \"y\": 171 }, { \"x\": 781, \"y\": 285 }, { \"x\": 772, \"y\": 321 }, { \"x\": 772, \"y\": 319 }, { \"x\": 774, \"y\": 283 }, { \"x\": 774, \"y\": 240 }, { \"x\": 772, \"y\": 234 }, { \"x\": 766, \"y\": 249 }, { \"x\": 744, \"y\": 300 }, { \"x\": 737, \"y\": 308 }, { \"x\": 732, \"y\": 307 }, { \"x\": 730, \"y\": 307 }, { \"x\": 735, \"y\": 313 }, { \"x\": 751, \"y\": 317 }, { \"x\": 761, \"y\": 317 }, { \"x\": 770, \"y\": 318 }, { \"x\": 782, \"y\": 320 }, { \"x\": 782, \"y\": 322 }, { \"x\": 781, \"y\": 322 }, { \"x\": 754, \"y\": 321 }, { \"x\": 704, \"y\": 321 }, { \"x\": 651, \"y\": 317 }, { \"x\": 580, \"y\": 311 }, { \"x\": 528, \"y\": 310 }, { \"x\": 490, \"y\": 307 }, { \"x\": 456, \"y\": 304 }, { \"x\": 424, \"y\": 307 }, { \"x\": 396, \"y\": 319 }, { \"x\": 378, \"y\": 322 }, { \"x\": 359, \"y\": 322 }, { \"x\": 351, \"y\": 323 }, { \"x\": 356, \"y\": 325 }, { \"x\": 435, \"y\": 340 }, { \"x\": 556, \"y\": 348 }, { \"x\": 716, \"y\": 341 }, { \"x\": 786, \"y\": 338 }, { \"x\": 844, \"y\": 331 }, { \"x\": 855, \"y\": 330 }, { \"x\": 860, \"y\": 333 }, { \"x\": 871, \"y\": 339 }, { \"x\": 889, \"y\": 345 }, { \"x\": 900, \"y\": 347 }, { \"x\": 904, \"y\": 348 }, { \"x\": 908, \"y\": 351 }, { \"x\": 917, \"y\": 359 }, { \"x\": 922, \"y\": 363 }, { \"x\": 921, \"y\": 363 } ] ]";
        signatureWithConsecutiveDuplicates = "[ [ { \"x\": 448, \"y\": 212 }, { \"x\": 451, \"y\": 245 },{ \"x\": 451, \"y\": 245 } ,{ \"x\": 451, \"y\": 245 } ,{ \"x\": 451, \"y\": 247 } ] ]";
    }

    @Test
    public void testSignatureDocumentGenerationNotNull() {
        List<List<Point>> points = deserializeSignature(signature);
        SignatureSVG doc = new SignatureSVG(points);
        byte[] byteArr = doc.generateInMemoryDocument();

        assertNotNull(byteArr);
    }

    @Test
    public void testRemoveConsecutiveDuplicatesFromSignature() {
        List<List<Point>> points = deserializeSignature(signatureWithConsecutiveDuplicates);
        List<List<Point>> removedDuplicates = GenericApplicationToDocumentUserMapper
                .removeConsecutiveDuplicatePoints(points);

        List<List<Point>> sigantureWithoutDuplicates = Lists.newArrayList();
        List<Point> tmp = Lists.newArrayList();
        tmp.add(new Point(448, 212));
        tmp.add(new Point(451, 245));
        tmp.add(new Point(451, 247));
        sigantureWithoutDuplicates.add(tmp);

        assertEquals(removedDuplicates, sigantureWithoutDuplicates);
    }

    @Test
    public void testPngGenerationNotNull() {
        List<List<Point>> points = deserializeSignature(signature);
        byte[] byteArr = GenericApplicationToDocumentUserMapper.getByteArrayImageFromPoints(points);

        assertNotNull(byteArr);
    }

    private List<List<Point>> deserializeSignature(String sig) {

        return SerializationUtils.deserializeFromString(sig, LIST_OF_LIST_OF_POINTS);
    }

    // use this function to export the file on a path ("/tmp/sig.svg")
    private void writeSvgFileToDisk(byte[] arr, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(arr);
            fos.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
