package com.mobilegt.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {

	static final String algorithmStr = "AES/ECB/PKCS5Padding";

	//static boolean isInited = false;


	private static byte[] getKey(String password) {
		byte[] rByte = null;
		if (password != null) {
			rByte = password.getBytes();
		} else {
			rByte = new byte[24];
		}
		return rByte;
	}

	// 注意: 这里的password(秘钥必须是16位的)
	private static final String keyBytes = "abcdefgabcdefg12";

	// 传入要加密的文件路径，加密后输出的路径
	public static void main(String srcPath, String AESPath) {
		try {
			decrypt(srcPath, AESPath);
		} catch (GeneralSecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static File mkdirFiles(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		file.createNewFile();
		if (!file.exists()) {
			file.mkdirs();
		}

		return file;
	}

	public static void decrypt(String srcFile, String destFile)
			throws GeneralSecurityException, IOException {

		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(mkdirFiles(destFile));
			crypt(fis, fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}

	private static void crypt(InputStream in, OutputStream out)
			throws IOException, GeneralSecurityException {
		/*
		 * ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		 * byte[] buff = new byte[1024]; // buff用于存放循环读取的临时数据 int rc = 0; while
		 * ((rc = in.read(buff, 0, 1024)) > 0) { swapStream.write(buff, 0, rc);
		 * } byte[] in_byte = swapStream.toByteArray(); // in_byte为转换之后的结果
		 * byte[] out_AES = null; byte[] keyStr = getKey(keyBytes);
		 * SecretKeySpec key = new SecretKeySpec(keyStr, "AES"); Cipher cipher =
		 * Cipher.getInstance(algorithmStr);// algorithmStr
		 * cipher.init(Cipher.ENCRYPT_MODE, key);// ʼ out_AES =
		 * cipher.doFinal(in_byte);
		 * 
		 * // byte[] out_AES = encrypt(in_byte, keyBytes); //加密
		 * out.write(out_AES);
		 */

		byte[] keyStr = getKey(keyBytes);
		SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
		Cipher cipher = Cipher.getInstance(algorithmStr);// algorithmStr
		cipher.init(Cipher.ENCRYPT_MODE, key);// ʼ
		int blockSize = cipher.getBlockSize() * 1000;
		int outputSize = cipher.getOutputSize(blockSize);

		byte[] inBytes = new byte[blockSize];
		byte[] outBytes = new byte[outputSize];

		int inLength = 0;
		boolean more = true;
		while (more) {
			inLength = in.read(inBytes);
			if (inLength == blockSize) {
				int outLength = cipher.update(inBytes, 0, blockSize, outBytes);
				out.write(outBytes, 0, outLength);
			} else {
				more = false;
			}
		}
		if (inLength > 0)
			outBytes = cipher.doFinal(inBytes, 0, inLength);
		else
			outBytes = cipher.doFinal();
		out.write(outBytes);
	}
}
