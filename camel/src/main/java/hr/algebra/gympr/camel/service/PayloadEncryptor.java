package hr.algebra.gympr.camel.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service("payloadEncryptor")
public class PayloadEncryptor {
    
    private static final String ALGORITHM = "AES";

    @Value("${gympr.payload.encryption-key:GymPRSecuredKey15}")
    private String encryptionKey;

    public String encrypt(String body) {
        if (body == null) return null;
        try {
            byte[] keyBytes = encryptionKey.getBytes("UTF-8");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(body.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.err.println("Encryption failed: " + e.getMessage());
            return body;
        }
    }

    public String decrypt(String encryptedBody) {
        if (encryptedBody == null) return null;
        try {
            byte[] keyBytes = encryptionKey.getBytes("UTF-8");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedBody);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            System.err.println("Decryption failed: " + e.getMessage());
            return encryptedBody;
        }
    }
}
