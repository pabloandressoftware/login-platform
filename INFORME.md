# Proyecto 6 – Login para una plataforma (Java + PBKDF2)

**Integrantes:** pabloandressoftware, Stick Martinez

## Cómo compilar y ejecutar
```bash
javac -d out src/login/*.java
java -cp out login.Main            # usa ./users.txt (o pasar otra ruta como argumento)
```
Requiere Java 11+ y no usa librerías externas, solo la API criptográfica estándar de Java.

## Funcionalidad
- **Administrador (único):** se crea en el primer arranque. Puede listar usuarios, eliminar un usuario y poner en blanco la contraseña de un usuario. No puede operar sobre su propia cuenta y no se puede crear un segundo administrador (el registro solo crea usuarios comunes).
- **Usuarios comunes:** se registran, consultan su último login y cambian su contraseña.
- Al entrar con una contraseña en blanco (reseteada), el sistema obliga a elegir una nueva.

## Cómo se hizo
| Clase | Responsabilidad |
|---|---|
| `PasswordHasher` | PBKDF2WithHmacSHA256, 210 000 iteraciones, salt de 16 bytes (`SecureRandom`), clave de 256 bits; verificación con `MessageDigest.isEqual` (tiempo constante). |
| `User` | Datos del usuario, rol, cambio/comprobación/blanqueo de contraseña. |
| `UserStore` | Persistencia en `users.txt`, una línea por usuario: `nombre;ROL;iteraciones;salt;hash;ultimoLogin` (Base64). Guardado atómico (temporal + rename). |
| `Main` | Menús de consola, validaciones y lectura de contraseñas sin eco (`Console.readPassword`). |

Decisiones de seguridad:
- Cada contraseña tiene su **propio salt**; se regenera en cada cambio de contraseña.
- Las iteraciones se guardan junto al hash para poder subirlas en el futuro sin invalidar cuentas.
- Mensaje único "Credenciales inválidas" y hash "de relleno" si el usuario no existe, para no revelar qué usuarios existen ni por tiempo de respuesta.
- Contraseña en blanco = salt/hash `-`; solo se acepta la contraseña vacía en ese estado.
- Las contraseñas se manejan como `char[]` y se limpian los `PBEKeySpec`.
- "Último login" mostrado = el de la sesión anterior (se actualiza al entrar).

## Dificultades
- _(Completar con la experiencia real del equipo; ideas: elegir el número de iteraciones, representar la contraseña en blanco sin guardar texto plano, `Console` es `null` en IDEs, evitar enumeración de usuarios.)_

## Conclusiones
- PBKDF2 con salt único y muchas iteraciones hace costoso el ataque por fuerza bruta/diccionario y evita ataques con tablas precalculadas (rainbow tables) y la detección de contraseñas repetidas.
- La API de Java (`SecretKeyFactory`, `PBEKeySpec`, `SecureRandom`) basta para un esquema de autenticación correcto sin dependencias.
- Mejoras posibles: bloqueo tras intentos fallidos, política de contraseñas más fuerte, Argon2/bcrypt, y cifrar el archivo de usuarios.
