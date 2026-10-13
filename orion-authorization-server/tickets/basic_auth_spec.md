# Especificación: proteger el CRUD de usuarios con HTTP Basic (AuthAdmin)

**Proyecto:** SOLO `orion-authorization-server` (:9000)
**No tocar:** `orion-resource-server` ni `orion-frontend`.

## Objetivo

Hoy las rutas que crean y administran usuarios están abiertas. Hay que protegerlas con **HTTP Basic** usando **un solo usuario en memoria**:

- Usuario: `AuthAdmin`
- Contraseña por defecto: `secret1234`

Este usuario NO existe en la base de datos. Solo sirve para llamar al CRUD de usuarios desde Postman.

## Paso 0: investigar antes de escribir código

1. Localiza el controller que crea usuarios y todas las rutas de ese CRUD (crear, listar, bloquear, etc.).
2. Localiza la `SecurityFilterChain` que ya existe (`AuthorizationServerConfig` tiene la del OAuth con `@Order(1)`) y la que tiene el `formLogin`.
3. Repórtame las rutas encontradas antes de continuar.

## Qué crear

**Una sola clase nueva:** `BasicAuthSecurityConfig`
**Paquete:** `com.debuggeandoideas.orion_authorization_server.config`

Contenido:

1. Constantes (nada de strings sueltos): rutas del CRUD, rol `AUTH_ADMIN`, y el orden del chain.
2. Dos propiedades con `@Value` a nivel de clase y con valor por defecto, sin tocar `application.yml`:
   - `@Value("${orion.admin.username:AuthAdmin}")`
   - `@Value("${orion.admin.password:secret1234}")`
3. Un `@Bean SecurityFilterChain basicAuthFilterChain(HttpSecurity http, PasswordEncoder passwordEncoder)` con:
   - `@Order` **entre** el chain OAuth (`@Order(1)`) y el chain del login. Si el del login no tiene `@Order`, ponle el valor más bajo posible solo si es indispensable y avísame.
   - `http.securityMatcher(...)` con las rutas del CRUD de usuarios (todas, incluida la de bloquear).
   - `authorizeHttpRequests(auth -> auth.anyRequest().hasRole(ROL))`
   - `httpBasic(Customizer.withDefaults())`
   - `sessionManagement` en `SessionCreationPolicy.STATELESS`
   - `csrf(AbstractHttpConfigurer::disable)`
   - `http.authenticationManager(...)` con un `ProviderManager` propio (ver siguiente punto).
4. El usuario en memoria y su `AuthenticationManager` **dentro del método del chain**, como variables locales (NO como `@Bean`):
   - `UserDetails` con `User.withUsername(username).password(passwordEncoder.encode(password)).roles(ROL).build()`
   - `InMemoryUserDetailsManager` con ese usuario
   - `DaoAuthenticationProvider` con ese manager y el `PasswordEncoder` existente
   - `new ProviderManager(provider)`

## Imports exactos

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
```

Nota: según la versión de Spring Security del proyecto, `DaoAuthenticationProvider` puede recibir el `UserDetailsService` en el constructor o por `setUserDetailsService`. Usa la forma que compile.

## Qué modificar

- **Nada más**, salvo que el `@Order` del chain del login choque con el nuevo. En ese caso, solo ajusta ese número y dímelo.
- Reutiliza el `PasswordEncoder` que ya existe (el de `{bcrypt}`). No crees otro.

## Qué NO hacer

- NO declarar `InMemoryUserDetailsManager` ni ningún `UserDetailsService` como `@Bean`: chocaría con el de JPA y rompería el login de los usuarios reales.
- NO tocar el chain OAuth (`@Order(1)`), `RoleTokenConfig`, `RegisteredClient`, `RoleScopePolicy`, el `UserDetailsService` de JPA ni el `PasswordEncoder`.
- NO proteger `/login`, `/oauth2/**` ni `/.well-known/**`: el `securityMatcher` solo cubre las rutas del CRUD.
- NO guardar `AuthAdmin` en la base de datos.
- NO usar sesión (nada de `JSESSIONID` en este chain).
- NO crear tests. Verifica solo con `mvn -q compile`.
- NO añadir `application.yml`. Los valores van con `@Value` y defaults.
- NO tocar el `LoginAttemptListener` (bloqueo por intentos). `AuthAdmin` no está en la BD, así que el listener no lo afecta.


## Prueba en Postman (Authorization → Basic Auth)

| Caso | Resultado esperado |
|---|---|
| Crear usuario sin credenciales | `401` con `WWW-Authenticate: Basic` |
| Crear usuario con `AuthAdmin` + clave incorrecta | `401` |
| Crear usuario con `AuthAdmin` / `secret1234` | `200` / `201` |
| Bloquear usuario con `AuthAdmin` / `secret1234` | OK |
| Login en `/login` con `AuthAdmin` | Falla (no existe en la BD) |
| Usuario de la BD (manager, user) por Basic en el CRUD | `401` |
| Flujo OAuth normal (`/oauth2/authorize`, `/oauth2/token`) | Sigue igual |

## Al terminar

Dame una lista corta de:
1. Clases nuevas.
2. Clases modificadas (si hubo alguna).
3. Las rutas del CRUD que quedaron protegidas.