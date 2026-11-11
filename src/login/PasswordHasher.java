package login;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Hashing de contraseñas con PBKDF2 (HMAC-SHA256) y salt aleatorio por usuario.
 * Usa únicamente la API criptográfica estándar de Java (JCA).
 */
public final class PasswordHasher {

    /** Iteraciones de PBKDF2 (recomendación OWASP 2023 para HMAC-SHA256). */
    public static final int ITERATIONS = 210_000;
    /** Tamaño del salt en bytes (128 bits). */
    public static final int SALT_BYTES = 16;
    /** Tamaño del hash derivado en bits. */
    public static final int KEY_BITS = 256;

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {}

    /** Genera un salt criptográficamente seguro. */
    public static byte[] newSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return salt;
    }

    /** Deriva el hash PBKDF2 de la contraseña con el salt e iteraciones dados. */
    public static byte[] hash(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 no disponible", e);
        } finally {
            spec.clearPassword(); // borra la copia interna de la contraseña
        }
    }

    /**
     * Verifica una contraseña contra un hash almacenado.
     * La comparación es en tiempo constante para evitar ataques de temporización.
     */
    public static boolean verify(char[] password, byte[] salt, int iterations, byte[] expected) {
        byte[] actual = hash(password, salt, iterations);
        boolean ok = MessageDigest.isEqual(actual, expected);
        Arrays.fill(actual, (byte) 0);
        return ok;
    }

    public static String toB64(byte[] data) { return Base64.getEncoder().encodeToString(data); }

    public static byte[] fromB64(String s) { return Base64.getDecoder().decode(s); }
}
