package login;

import java.time.LocalDateTime;

/**
 * Registro de un usuario. Una contraseña "en blanco" (reseteada por el admin)
 * se representa con salt/hash nulos: el usuario entra con contraseña vacía y
 * el sistema le obliga a elegir una nueva.
 */
public class User {
    public enum Role { ADMIN, USER }

    private final String name;
    private final Role role;
    private int iterations;
    private byte[] salt;          // null => contraseña en blanco
    private byte[] hash;          // null => contraseña en blanco
    private LocalDateTime lastLogin; // null => nunca ha iniciado sesión

    public User(String name, Role role, int iterations, byte[] salt, byte[] hash, LocalDateTime lastLogin) {
        this.name = name;
        this.role = role;
        this.iterations = iterations;
        this.salt = salt;
        this.hash = hash;
        this.lastLogin = lastLogin;
    }

    public String getName() { return name; }
    public Role getRole() { return role; }
    public int getIterations() { return iterations; }
    public byte[] getSalt() { return salt; }
    public byte[] getHash() { return hash; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public boolean isBlank() { return hash == null; }

    public void setLastLogin(LocalDateTime t) { this.lastLogin = t; }

    /** Establece una nueva contraseña con un salt nuevo. */
    public void setPassword(char[] password) {
        this.salt = PasswordHasher.newSalt();
        this.iterations = PasswordHasher.ITERATIONS;
        this.hash = PasswordHasher.hash(password, salt, iterations);
    }

    /** Pone la contraseña en blanco. */
    public void blankPassword() {
        this.salt = null;
        this.hash = null;
    }

    /** Comprueba la contraseña (la vacía solo es válida si está en blanco). */
    public boolean checkPassword(char[] password) {
        if (isBlank()) return password.length == 0;
        return PasswordHasher.verify(password, salt, iterations, hash);
    }
}
