package login;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Interfaz de consola de la plataforma. Hay un único administrador (creado en
 * el primer arranque) y usuarios comunes que se registran por su cuenta.
 */
public class Main {
    private static final Pattern NAME_RE = Pattern.compile("[A-Za-z0-9_]{3,20}");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final Scanner IN = new Scanner(System.in);
    private static final Console CONSOLE = System.console();

    private static UserStore store;

    public static void main(String[] args) throws IOException {
        store = new UserStore(Path.of(args.length > 0 ? args[0] : "users.txt"));
        if (!store.hasAdmin()) firstRunSetup();

        while (true) {
            System.out.println("\n=== Plataforma ===\n1) Iniciar sesión\n2) Registrarse\n0) Salir");
            switch (ask("Opción: ")) {
                case "1" -> login();
                case "2" -> register();
                case "0" -> { return; }
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    // ---------- flujos ----------

    /** Crea el único administrador la primera vez que se ejecuta. */
    private static void firstRunSetup() throws IOException {
        System.out.println("Primer arranque: cree la cuenta del administrador.");
        String name = askName();
        char[] pw = askNewPassword();
        User admin = new User(name, User.Role.ADMIN, 0, null, null, null);
        admin.setPassword(pw);
        store.add(admin);
        store.save();
    }

    private static void register() throws IOException {
        String name = askName();
        if (store.get(name) != null) { System.out.println("Ese nombre ya existe."); return; }
        User u = new User(name, User.Role.USER, 0, null, null, null);
        u.setPassword(askNewPassword());
        store.add(u);
        store.save();
        System.out.println("Usuario creado.");
    }

    private static void login() throws IOException {
        String name = ask("Usuario: ");
        char[] pw = askSecret("Contraseña: ");
        User u = store.get(name);
        // Mismo mensaje y mismo trabajo (hash) exista o no el usuario: evita enumerar usuarios.
        if (u == null) {
            PasswordHasher.hash(pw, PasswordHasher.newSalt(), PasswordHasher.ITERATIONS);
            System.out.println("Credenciales inválidas.");
            return;
        }
        if (!u.checkPassword(pw)) { System.out.println("Credenciales inválidas."); return; }

        LocalDateTime previous = u.getLastLogin();
        if (u.isBlank()) {
            System.out.println("Su contraseña fue restablecida; debe elegir una nueva.");
            u.setPassword(askNewPassword());
        }
        u.setLastLogin(LocalDateTime.now());
        store.save();

        if (u.getRole() == User.Role.ADMIN) adminMenu(u);
        else userMenu(u, previous);
    }

    private static void adminMenu(User admin) throws IOException {
        while (true) {
            System.out.println("\n--- Administrador: " + admin.getName() + " ---\n"
                + "1) Listar usuarios\n2) Eliminar usuario\n3) Poner en blanco contraseña\n0) Cerrar sesión");
            switch (ask("Opción: ")) {
                case "1" -> store.all().forEach(u -> System.out.println(" - " + u.getName()
                        + (u.getRole() == User.Role.ADMIN ? " (admin)" : "")));
                case "2" -> {
                    User t = targetUser();
                    if (t != null) { store.remove(t.getName()); store.save(); System.out.println("Eliminado."); }
                }
                case "3" -> {
                    User t = targetUser();
                    if (t != null) { t.blankPassword(); store.save(); System.out.println("Contraseña en blanco."); }
                }
                case "0" -> { return; }
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    /** Pide un usuario objetivo; el administrador no puede operar sobre sí mismo. */
    private static User targetUser() {
        User t = store.get(ask("Nombre del usuario: "));
        if (t == null) { System.out.println("No existe."); return null; }
        if (t.getRole() == User.Role.ADMIN) { System.out.println("No permitido sobre el administrador."); return null; }
        return t;
    }

    private static void userMenu(User u, LocalDateTime previous) throws IOException {
        while (true) {
            System.out.println("\n--- Usuario: " + u.getName() + " ---\n"
                + "1) Consultar último login\n2) Cambiar contraseña\n0) Cerrar sesión");
            switch (ask("Opción: ")) {
                case "1" -> System.out.println("Último login anterior a esta sesión: "
                        + (previous == null ? "(es su primer login)" : previous.format(FMT)));
                case "2" -> {
                    char[] old = askSecret("Contraseña actual: ");
                    if (!u.checkPassword(old)) { System.out.println("Contraseña incorrecta."); break; }
                    u.setPassword(askNewPassword());
                    store.save();
                    System.out.println("Contraseña cambiada.");
                }
                case "0" -> { return; }
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    // ---------- entrada ----------

    private static String ask(String prompt) {
        System.out.print(prompt);
        return IN.hasNextLine() ? IN.nextLine().trim() : "0";
    }

    /** Lee sin eco si hay consola; si no (IDE/pipe) recurre a Scanner. */
    private static char[] askSecret(String prompt) {
        if (CONSOLE != null) return CONSOLE.readPassword(prompt);
        System.out.print(prompt);
        return (IN.hasNextLine() ? IN.nextLine() : "").toCharArray();
    }

    private static String askName() {
        while (true) {
            String n = ask("Nombre de usuario (3-20, letras/números/_): ");
            if (NAME_RE.matcher(n).matches()) return n;
            System.out.println("Nombre inválido.");
        }
    }

    private static char[] askNewPassword() {
        while (true) {
            char[] a = askSecret("Nueva contraseña (mín. 8 caracteres): ");
            char[] b = askSecret("Repita la contraseña: ");
            if (a.length < 8) System.out.println("Muy corta.");
            else if (!java.util.Arrays.equals(a, b)) System.out.println("No coinciden.");
            else return a;
        }
    }
}
