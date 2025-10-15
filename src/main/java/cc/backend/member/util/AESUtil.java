package cc.backend.member.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil { //ECB, CBC, GCM 중 GCM 사용(동일 평문도 다른 암호문 생성)
    private static final String AES = "AES";
    private static final String AES_GCM_NOPADDING = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;   // GCM 권장 12바이트
    private static final int TAG_SIZE = 128; // 인증 태그 128비트

    private static final SecureRandom RANDOM = new SecureRandom();

    // 환경변수에서 키 로딩
    private static final SecretKey SECRET_KEY;
    static {
        String encodedKey = System.getenv("AES_KEY"); // 환경변수 AES_KEY(base64로 인코딩된 상태)
        if (encodedKey == null || encodedKey.isEmpty()) {
            throw new IllegalStateException("AES_KEY 환경 변수 부재");
        }
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        SECRET_KEY = new SecretKeySpec(keyBytes, AES);
    }

    public static String encrypt(String plainText) throws Exception {
        byte[] iv = new byte[IV_SIZE];
        RANDOM.nextBytes(iv); //iv를 랜덤으로 생성

        Cipher cipher = Cipher.getInstance(AES_GCM_NOPADDING);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, spec);

        byte[] ciphertext = cipher.doFinal(plainText.getBytes());

        // iv + ciphertext = encrypted
        byte[] encrypted = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encrypted, 0, iv.length);
        System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encodedEncrypted) throws Exception {
        byte[] encrypted = Base64.getDecoder().decode(encodedEncrypted);

        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(encrypted, 0, iv, 0, IV_SIZE);

        byte[] ciphertext = new byte[encrypted.length - IV_SIZE];
        System.arraycopy(encrypted, IV_SIZE, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(AES_GCM_NOPADDING);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, spec);

        byte[] decrypted = cipher.doFinal(ciphertext);
        return new String(decrypted);
    }
}
