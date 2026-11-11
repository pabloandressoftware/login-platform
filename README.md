# Login para una plataforma (Java + PBKDF2)

Proyecto de criptografía: gestión de usuarios y contraseñas de una plataforma, usando la API criptográfica de Java.

## Integrantes
- Pablo Guzman Alarcon
- Stick Martinez

## Descripción
- **Administrador (único):** lista usuarios, elimina usuarios y pone en blanco la contraseña de un usuario.
- **Usuarios comunes:** consultan su último login (fecha/hora) y cambian su contraseña.
- Las contraseñas se almacenan en `users.txt` como hash **PBKDF2WithHmacSHA256** (210 000 iteraciones) con **salt** aleatorio de 16 bytes por usuario. Nunca se guardan en claro.

## Compilar y ejecutar
Requiere Java 11 o superior.
```bash
javac -d out src/login/*.java
java -cp out login.Main            # usa ./users.txt (o pasar otra ruta como argumento)
```
En el primer arranque se pide crear la cuenta del administrador.

## Estructura
| Archivo | Función |
|---|---|
| `src/login/PasswordHasher.java` | PBKDF2, salt y verificación en tiempo constante |
| `src/login/User.java` | Modelo de usuario y roles |
| `src/login/UserStore.java` | Persistencia en archivo de texto |
| `src/login/Main.java` | Menús de consola |

Más detalle en [INFORME.md](INFORME.md).
