package se.tink.backend.aggregation.agents.utils.crypto;

import com.google.common.primitives.Bytes;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.KeyAgreement;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

public class EllipticCurve {
    public static byte[] diffieHellmanDeriveKey(PrivateKey privKey, PublicKey pubKey) {
        try {
            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(privKey);
            ka.doPhase(pubKey, true);
            return ka.generateSecret();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /** Generates a shared secret using ECDH with the x and y points concatenated. */
    public static byte[] diffieHellmanDeriveKeyConcatXY(
            PrivateKey privateKey, PublicKey publicKey) {
        try {
            ECPrivateKeyParameters privateKeyParameters =
                    (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter(privateKey);
            ECPublicKeyParameters publicKeyParameters =
                    (ECPublicKeyParameters) ECUtil.generatePublicKeyParameter(publicKey);
            ECDHConcatXYAgreement agreement = new ECDHConcatXYAgreement();
            agreement.init(privateKeyParameters);
            return agreement.calculateAgreement(publicKeyParameters);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    public static KeyPair generateKeyPair(String curveName) {
        try {
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(curveName);
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", "BC");
            kpg.initialize(ecSpec, new SecureRandom());
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException
                | InvalidAlgorithmParameterException
                | NoSuchProviderException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static PublicKey convertPEMtoPublicKey(byte[] pubKeyBytes) {
        try {
            KeyFactory factory = KeyFactory.getInstance("ECDSA", "BC");
            return factory.generatePublic(new X509EncodedKeySpec(pubKeyBytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] convertPublicKeyToPoint(KeyPair keyPair, boolean compressed) {
        return convertPublicKeyToPoint((ECPublicKey) keyPair.getPublic(), compressed);
    }

    public static byte[] convertPublicKeyToPoint(ECPublicKey publicKey, boolean compressed) {
        ECPoint q = publicKey.getQ();
        return new ECPoint.Fp(q.getCurve(), q.getX(), q.getY(), compressed).getEncoded();
    }

    public static ECPrivateKey convertPointToPrivateKey(byte[] prvKeyBytes, String curveName) {
        ECNamedCurveParameterSpec ecNamedCurveParameterSpec =
                ECNamedCurveTable.getParameterSpec(curveName);
        java.security.spec.EllipticCurve ellipticCurve =
                EC5Util.convertCurve(
                        ecNamedCurveParameterSpec.getCurve(), ecNamedCurveParameterSpec.getSeed());
        java.security.spec.ECParameterSpec ecParameterSpec =
                EC5Util.convertSpec(ellipticCurve, ecNamedCurveParameterSpec);
        java.security.spec.ECPrivateKeySpec privateKeySpec =
                new java.security.spec.ECPrivateKeySpec(
                        new BigInteger(1, prvKeyBytes), ecParameterSpec);
        try {
            KeyFactory kf = KeyFactory.getInstance("EC", "BC");
            return (ECPrivateKey) kf.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ECPublicKey convertPointToPublicKey(byte[] pubKeybytes, String curveName) {
        ECNamedCurveParameterSpec ecNamedCurveParameterSpec =
                ECNamedCurveTable.getParameterSpec(curveName);
        java.security.spec.EllipticCurve ellipticCurve =
                EC5Util.convertCurve(
                        ecNamedCurveParameterSpec.getCurve(), ecNamedCurveParameterSpec.getSeed());
        java.security.spec.ECPoint ecPoint = ECPointUtil.decodePoint(ellipticCurve, pubKeybytes);
        java.security.spec.ECParameterSpec ecParameterSpec =
                EC5Util.convertSpec(ellipticCurve, ecNamedCurveParameterSpec);
        java.security.spec.ECPublicKeySpec publicKeySpec =
                new java.security.spec.ECPublicKeySpec(ecPoint, ecParameterSpec);
        try {
            KeyFactory kf = KeyFactory.getInstance("EC", "BC");
            return (ECPublicKey) kf.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    public static PublicKey getPublicKeyFromCurveAndPoints(String curveName, byte[] x, byte[] y) {
        BigInteger X = BigIntegers.fromUnsignedByteArray(x);
        BigInteger Y = BigIntegers.fromUnsignedByteArray(y);

        ECNamedCurveParameterSpec ecNamedCurveParameterSpec =
                ECNamedCurveTable.getParameterSpec(curveName);
        java.security.spec.EllipticCurve ellipticCurve =
                EC5Util.convertCurve(
                        ecNamedCurveParameterSpec.getCurve(), ecNamedCurveParameterSpec.getSeed());
        java.security.spec.ECPoint ecPoint = new java.security.spec.ECPoint(X, Y);
        java.security.spec.ECParameterSpec ecParameterSpec =
                EC5Util.convertSpec(ellipticCurve, ecNamedCurveParameterSpec);
        java.security.spec.ECPublicKeySpec publicKeySpec =
                new java.security.spec.ECPublicKeySpec(ecPoint, ecParameterSpec);
        try {
            KeyFactory kf = KeyFactory.getInstance("EC", "BC");
            return kf.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class ECDHConcatXYAgreement {
        private static final X9IntegerConverter converter = new X9IntegerConverter();

        private ECPrivateKeyParameters key;

        public void init(CipherParameters key) {
            this.key = (ECPrivateKeyParameters) key;
        }

        public int getFieldSize() {
            return (key.getParameters().getCurve().getFieldSize() + 7) / 8;
        }

        public byte[] calculateAgreement(CipherParameters pubKey) {
            ECPublicKeyParameters pub = (ECPublicKeyParameters) pubKey;
            if (!pub.getParameters().equals(key.getParameters())) {
                throw new IllegalStateException("ECDH public key has wrong domain parameters");
            }

            // Always perform calculations on the exact curve specified by our private key's
            // parameters
            ECPoint pubPoint =
                    key.getParameters().getCurve().decodePoint(pub.getQ().getEncoded(false));
            if (pubPoint.isInfinity()) {
                throw new IllegalStateException("Infinity is not a valid public key for ECDH");
            }

            ECPoint P = pubPoint.multiply(key.getD()).normalize();

            if (P.isInfinity()) {
                throw new IllegalStateException("Infinity is not a valid agreement value for ECDH");
            }

            byte[] x = bigIntToBytes(P.getAffineXCoord().toBigInteger());
            byte[] y = bigIntToBytes(P.getAffineYCoord().toBigInteger());
            return Bytes.concat(x, y);
        }

        protected byte[] bigIntToBytes(BigInteger r) {
            return converter.integerToBytes(
                    r, converter.getByteLength(key.getParameters().getCurve()));
        }
    }
}
