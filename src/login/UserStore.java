package login;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Persistencia en archivo de texto. Una línea por usuario:
 * <pre>nombre;ROL;iteraciones;saltBase64;hashBase64;ultimoLogin</pre>
 * Salt/hash valen "-" si la contraseña está en blanco; ultimoLogin vale "-"
 * si nunca ha entrado. Nunca se guardan contraseñas en claro.
 */
public class UserStore {
    private final Path file;
    private final Map<String, User> users = new TreeMap<>();

    public UserStore(Path file) throws IOException {
        this.file = file;
        if (Files.exists(file)) load();
    }

    private void load() throws IOException {
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            String[] f = line.split(";", -1);
            if (f.length != 6) throw new IOException("Línea corrupta en " + file + ": " + line);
            boolean blank = f[3].equals("-");
            users.put(f[0], new User(
                f[0], User.Role.valueOf(f[1]), Integer.parseInt(f[2]),
                blank ? null : PasswordHasher.fromB64(f[3]),
                blank ? null : PasswordHasher.fromB64(f[4]),
                f[5].equals("-") ? null : LocalDateTime.parse(f[5])));
        }
    }

    /** Guarda de forma atómica (archivo temporal + rename) para no corromper datos. */
    public void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : users.values()) {
            lines.add(String.join(";", u.getName(), u.getRole().name(),
                String.valueOf(u.getIterations()),
                u.isBlank() ? "-" : PasswordHasher.toB64(u.getSalt()),
                u.isBlank() ? "-" : PasswordHasher.toB64(u.getHash()),
                u.getLastLogin() == null ? "-" : u.getLastLogin().toString()));
        }
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(tmp, lines, StandardCharsets.UTF_8);
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public User get(String name) { return users.get(name); }
    public boolean isEmpty() { return users.isEmpty(); }
    public Collection<User> all() { return users.values(); }
    public boolean hasAdmin() { return users.values().stream().anyMatch(u -> u.getRole() == User.Role.ADMIN); }

    public void add(User u) { users.put(u.getName(), u); }
    public void remove(String name) { users.remove(name); }
}
