package com.cyk.t.protocol.alg.sm2;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;


/**
 * java 实现的GMT 0003.1 2 的SM2签名验签
 * on bc 1.64 but not use all
 *
 * @author WangChengyu
 * 2020/5/19 17:25
 */
public class GMTSM2 {
    private static GMTSM2 instance;

    public static GMTSM2 getInstance() {
        if (instance == null) {
            synchronized (GMTSM2.class) {
                if (instance == null) {
                    instance = new GMTSM2();
                }
            }
        }
        return instance;
    }

    final static BigInteger ZERO = BigInteger.valueOf(0L);
    final static BigInteger ONE = BigInteger.valueOf(1L);
    final static BigInteger TWO = BigInteger.valueOf(2L);
    final static BigInteger THREE = BigInteger.valueOf(3L);
    final static BigInteger FOUR = BigInteger.valueOf(4L);

    private static X9ECParameters x9ECParameters = GMNamedCurves.getByName("sm2p256v1");
    private static ECDomainParameters ecDomainParameters = new ECDomainParameters(x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN());
    private static ECParameterSpec ecParameterSpec = new ECParameterSpec(x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN());


    //正式参数(GMT 0003.5)
    public static String[] ecc_param = {
            "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF",
            "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC",
            "28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93",
            "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123",
            "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7",
            "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0"
    };

    BigInteger ecc_p;
    BigInteger ecc_a;
    BigInteger ecc_b;
    BigInteger ecc_n;
    BigInteger ecc_gx;
    BigInteger ecc_gy;
    ECCurve ecc_curve;
    ECPoint ecc_point_g;
    ECDomainParameters ecc_bc_spec;
    ECFieldElement ecc_gx_fieldelement;
    ECFieldElement ecc_gy_fieldelement;


    /**
     * 初始化 ECC 常量
     */
    private GMTSM2() {
        // 初始化参数
        this.ecc_p = new BigInteger(ecc_param[0], 16);
        this.ecc_a = new BigInteger(ecc_param[1], 16);
        this.ecc_b = new BigInteger(ecc_param[2], 16);
        this.ecc_n = new BigInteger(ecc_param[3], 16);
        this.ecc_gx = new BigInteger(ecc_param[4], 16);
        this.ecc_gy = new BigInteger(ecc_param[5], 16);

        // 初始化运算结构
        ecc_gx_fieldelement = new ECFieldElement.Fp(this.ecc_p, this.ecc_gx);
        ecc_gy_fieldelement = new ECFieldElement.Fp(this.ecc_p, this.ecc_gy);
        ecc_curve = new ECCurve.Fp(this.ecc_p, this.ecc_a, this.ecc_b);
        ecc_point_g = x9ECParameters.getG();

        ecc_bc_spec =  ecDomainParameters;
    }

    /**
     * org.bouncycastle.crypto.generators;
     * 生成基于boucycastle的ecc密钥结果
     *
     * @return
     */
    public AsymmetricCipherKeyPair genPairOnBouncycastle() {
        BigInteger da = genKinN();
        ECPoint p = ecc_bc_spec.getG().multiply(da);
        return new AsymmetricCipherKeyPair(new ECPublicKeyParameters(p, this.ecc_bc_spec), new ECPrivateKeyParameters(da, this.ecc_bc_spec));
    }

    /**
     * 生成密钥对 （Hex字符串的结果）
     *
     * @return {puk, pvk}
     */
    public String[] genPairOnString() {
        // 产生随机数
        BigInteger da = genKinN();
        byte[] skarr = da.toByteArray();
        byte[] bsk = new byte[32];
        int lsk = skarr.length;
        if (lsk < 32) {
            System.arraycopy(skarr, 0, bsk, 32 - lsk, lsk);
        } else {
            System.arraycopy(skarr, skarr.length - 32, bsk, 0, 32);
        }
        String hexPrivateKey = StrUtil.byteToHex(bsk);

        ECPoint p = ecc_bc_spec.getG().multiply(da);
        byte[] bpx = p.normalize().getXCoord().toBigInteger().toByteArray();
        byte[] bpy = p.normalize().getYCoord().toBigInteger().toByteArray();
        byte[] bpk = new byte[64];
 
        int lx = bpx.length;
        if (lx < 32) {
            System.arraycopy(bpx, 0, bpk, 32 - lx, lx);
        } else {
            System.arraycopy(bpx, lx - 32, bpk, 0, 32);
        }
        int ly = bpy.length;
        if (ly < 32) {
            System.arraycopy(bpy, 0, bpk, 32 - ly, ly);
        } else {
            System.arraycopy(bpy, ly - 32, bpk, 32, 32);
        }
        String hexPublicKey = "04" + StrUtil.byteToHex(bpk);
        return new String[]{hexPublicKey, hexPrivateKey};
    }

    /**
     * 根据私钥生成公钥
     */
    public String calcPkfrSk(byte[] sk){
        BigInteger da = new BigInteger(1, sk);
        ECPoint p = ecc_bc_spec.getG().multiply(da);
        byte[] bpx = p.normalize().getXCoord().toBigInteger().toByteArray();
        byte[] bpy = p.normalize().getYCoord().toBigInteger().toByteArray();
        byte[] bpk = new byte[64];
        int lx = bpx.length;
        if (lx < 32) {
            System.arraycopy(bpx, 0, bpk, 32 - lx, lx);
        } else {
            System.arraycopy(bpx, lx - 32, bpk, 0, 32);
        }
        int ly = bpy.length;
        if (ly < 32) {
            System.arraycopy(bpy, 0, bpk, 32 - ly, ly);
        } else {
            System.arraycopy(bpy, ly - 32, bpk, 32, 32);
        }
        return "04" + StrUtil.byteToHex(bpk);
    }


    /**
     * from P = k * G 得出曲线上的点（x, y）
     *
     * @param pvk
     * @return
     */
    private ECPoint calcPa(String pvk) {
        return ecc_bc_spec.getG().multiply(new BigInteger(pvk, 16));
    }

    /**
     * 取点（取0-n区间内）的随机数
     *
     * @return
     */
    private BigInteger genKinN() {
        // 随机数产生器
        SecureRandom random = new SecureRandom();
        // 产生0 - n之间的随机数
        BigInteger da;
        do {// generate k in 0 ~ n, mean da for privateKey
            do {
                da = new BigInteger(256, random);
            } while (da.compareTo(ZERO) <= 0);//da.equals(ZERO));
        } while (da.compareTo(ecc_n) >= 0);
        return da;
    }

    // ++++=====================================================++++



    /**
     * 签名（摘要，私钥）
     */
    public byte[] sm2Sign(byte[] hashData, byte[] pvk) {
        // check before calc
        if (hashData.length != 32) {
            throw new IllegalArgumentException("input digest length error: export 32, actual " + hashData.length);
        }
        if (pvk.length != 32) {
            throw new IllegalArgumentException("input key length error: export 32, actual " + pvk.length);
        }
        // e
        BigInteger e = new BigInteger(1, hashData);
        // k
        BigInteger k = null;
        ECPoint kp = null;
        BigInteger r = null;// 签名出参 r
        BigInteger s = null;// 签名出参 s
        BigInteger userD = new BigInteger(1, pvk);// 私钥d
        BigInteger ecc_n = this.ecc_n;
        do {
            do {
                // 随机数取点k in 0-n
                k = genKinN();
                kp = calcPa(k.toString(16)); // 得到kp(x,y)
                // 计算: r = (e+x) mod n
                r = e.add(kp.normalize().getXCoord().toBigInteger());
                r = r.mod(ecc_n);
                // 结果r等于0或（r+k）等于n则重算
            } while (r.equals(BigInteger.ZERO) || r.add(k).equals(ecc_n));

            // s=  (((1 + dA)~-1)  *  (k - r*da )) mod n
            BigInteger da_1 = userD.add(BigInteger.ONE);
            da_1 = da_1.modInverse(ecc_n);
            s = r.multiply(userD);
            s = k.subtract(s).mod(ecc_n);
            s = da_1.multiply(s).mod(ecc_n);
            // 结果s等于0则重算
        } while (s.equals(BigInteger.ZERO));
        try{
            ASN1Encodable[] arr = {new ASN1Integer(r), new ASN1Integer(s)};
            return (new DERSequence(arr)).getEncoded();
        }catch (Exception te){
            throw new RuntimeException("签名结果编码失败", te);
        }
    }

    /**
     * 验证签名（摘要， 签名值， 公钥）
     */
    public boolean sm2Verify(byte[] hashData, BigInteger r, BigInteger s, byte[] pk) {
        // check before verifySign
        if (hashData.length != 32) {
            throw new IllegalArgumentException("input digest length error: export 32, actual " + hashData.length);
        }

        if (pk.length != 65) {
            throw new IllegalArgumentException("input publicKey length error: export 65, actual " + pk.length);
        }
        // e
        BigInteger e = new BigInteger(1, hashData);

        // k
        ECPoint k;
        ECPoint G = null;
        ECPoint Pa = null;
        BigInteger t = null;
        BigInteger R = null;
        BigInteger ecc_n = null;

        Pa = ecc_curve.decodePoint(pk);

        G = this.ecc_bc_spec.getG();
        ecc_n = this.ecc_n;

        if (r.equals(BigInteger.ONE) || r.equals(ecc_n)) {
            return false;
        }
        if (s.equals(BigInteger.ONE) || s.equals(ecc_n)) {
            return false;
        }

        t = r.add(s).mod(ecc_n);
        if (t.equals(BigInteger.ZERO)) {
            return false;
        }

        //k(x,y) = s*G + t*Pa
        k = G.multiply(s).add(Pa.multiply(t));

        //R = (e+k.x) mod n
        R = e.add(k.normalize().getXCoord().toBigInteger()).mod(ecc_n);
        //R == r  true
        if (R.equals(r)) return true;
        return false;
    }





}
