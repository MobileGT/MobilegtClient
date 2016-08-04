package com.mobilegt.vpn;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by lenovo-pc on 2016/4/28.
 */
public class ToyVpnAESOperator {
    /*
     * 加密用的Key 可以用26个字母和数字组成
     * 此处使用AES-128-CBC加密模式，key需要为16位。
     */

    //    private String sKey = "0123456789abcdef";
    private String sKey = "test0rywang00000";
    //    private String ivParameter = "0123456789abcdef";
    private String ivParameter = "toyvpnserver0000";
    private Cipher encryptCipher=null;
    private Cipher decryptCipher=null;
    private static ToyVpnAESOperator instance = null;

    private ToyVpnAESOperator() {
        try {
            byte[] raw = sKey.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            encryptCipher = Cipher.getInstance("AES/CBC/NoPadding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            decryptCipher = Cipher.getInstance("AES/CBC/NoPadding");
            decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static ToyVpnAESOperator getInstance() {
        if (instance == null) {
            instance = new ToyVpnAESOperator();
        }
        return instance;
    }

    // 加密
    public byte[] encrypt(byte[] plainText) throws Exception {
//        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        int length=plainText.length;
        int paddingLength=16-length%16;
        byte[] plainBytes=new byte[length+paddingLength];
        System.arraycopy(plainText, 0, plainBytes, 0, length);
        for(int i=length;i<plainBytes.length;i++)
            plainBytes[i]=0;
        byte[] encrypted = encryptCipher.doFinal(plainBytes);
//        System.out.println("encrypted:" + bytes2hex(encrypted));
//        for(int i=0;i<encrypted.length;i++)
//            System.out.print(encrypted[i]+" ");
//        System.out.println("");
        // return new BASE64Encoder().encode(encrypted);//此处使用BASE64做转码。
        return encrypted;
    }

    // 解密
    public byte[] decrypt(byte[] encryptedText) {
        try {
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            //byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);//先用base64解密
            byte[] original = decryptCipher.doFinal(encryptedText);
            //String originalString = new String(original, "utf-8");
            return original;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        // 需要加密的字串
        String cSrc = "Now is the time for all good men to come to the aide..........";
        int length=cSrc.getBytes().length;
        System.out.println("加密前的字串是：" + cSrc + "(长度：" + length+")");

        ToyVpnAESOperator aesOperator=ToyVpnAESOperator.getInstance();
        // 加密
        long lStart = System.currentTimeMillis();
        byte[] encryptedBytes=aesOperator.encrypt(cSrc.getBytes());
        String enString = new String(encryptedBytes);

        System.out.println("加密后的字串是：" + enString);

        long lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("加密耗时：" + lUseTime + "毫秒");
        // 解密
        lStart = System.currentTimeMillis();
        byte[] decryptedBytes=aesOperator.decrypt(encryptedBytes);
        String DeString = new String(decryptedBytes);
        System.out.println("解密后的字串是：" + DeString);
        lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("解密耗时：" + lUseTime + "毫秒");

        cSrc="第二次加密测试";
        length=cSrc.getBytes().length;
        System.out.println("加密前的字串是：" + cSrc + "(长度：" + length+")");
        encryptedBytes=aesOperator.encrypt(cSrc.getBytes());
        // 加密
        lStart = System.currentTimeMillis();
        encryptedBytes=aesOperator.encrypt(cSrc.getBytes());
        enString = new String(encryptedBytes);

        System.out.println("加密后的字串是：" + enString);

        lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("加密耗时：" + lUseTime + "毫秒");
        // 解密
        lStart = System.currentTimeMillis();
        decryptedBytes=aesOperator.decrypt(encryptedBytes);
        DeString = new String(decryptedBytes);
        System.out.println("解密后的字串是：" + DeString);
        lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("解密耗时：" + lUseTime + "毫秒");
    }

    public static String bytes2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String temp = (Integer.toHexString(bytes[i] & 0XFF));
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            sb.append(temp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase();
    }

    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
}