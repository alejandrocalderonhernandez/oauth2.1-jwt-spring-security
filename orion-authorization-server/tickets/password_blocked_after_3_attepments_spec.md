# Bloqueo de cuenta tras 3 contraseñas incorrectas

Trabaja SOLO dentro de `orion-authorization-server`. No toques el Resource Server ni el frontend.
Sin tests. Verifica con `mvn -q compile`.

## Objetivo
Cuando un usuario falla la contraseña 3 veces seguidas, su cuenta queda bloqueada con el mismo
mecanismo que ya existe (la bandera de cuenta bloqueada que usa el endpoint de bloqueo y que
`UserDetailsService` ya mapea). Un login exitoso reinicia la cuenta de fallos.

## Antes de escribir código
1. Localiza el servicio de usuarios y el método que bloquea a un usuario (el que usa el endpoint
   de bloqueo). Dime cuál es.
2. Si ese método solo funciona por `id`, agrega en ese mismo servicio un método
   `lockByUsername(String username)` que busque al usuario y delegue en la MISMA lógica de
   bloqueo. No dupliques lógica. Si el usuario no existe, no hace nada y no lanza excepción.

## Clase nueva: `LoginAttemptListener`
Ubicación: el mismo paquete donde están las clases de configuración existentes
(`com.debuggeandoideas.orion_authorization_server.config`). No inventes paquetes nuevos.

Anotaciones e imports exactos:
- `org.springframework.stereotype.Component`
- `org.springframework.context.event.EventListener`
- `org.springframework.beans.factory.annotation.Value`
- `org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent`
- `org.springframework.security.authentication.event.AuthenticationSuccessEvent`
- `java.util.Map` y `java.util.concurrent.ConcurrentHashMap`

Comportamiento:
- Propiedad a nivel de clase con default, sin application.yml:
  `@Value("${orion.security.max-failed-attempts:3}") private int maxFailedAttempts;`
- Un `Map<String, Integer>` concurrente con los fallos por nombre de usuario.
- `@EventListener` sobre `AuthenticationFailureBadCredentialsEvent`: toma el nombre con
  `event.getAuthentication().getName()`, suma 1 (usa `merge`, es concurrente). Si llega a
  `maxFailedAttempts`: llama al método de bloqueo del servicio de usuarios y elimina la entrada
  del mapa.
- `@EventListener` sobre `AuthenticationSuccessEvent`: elimina la entrada de ese nombre.
- Sin strings sueltos: usa constantes para cualquier texto.

## Lo que NO debes hacer
- No modificar `SecurityFilterChain`, `UserDetailsService`, `PasswordEncoder`, `RegisteredClient`
  ni `RoleTokenConfig`.
- No crear application.yml ni tocar propiedades existentes.
- No desbloquear automáticamente: el desbloqueo sigue siendo el del endpoint existente.
- No contar ni bloquear usuarios que no existen.
- No registrar contraseñas en logs.
- No agregar columnas ni tablas: el contador vive en memoria.


## Al terminar
Da una lista corta de las clases nuevas y/o modificadas, y el nombre del método de bloqueo que
reutilizaste.

## Cómo lo voy a probar yo
3 contraseñas incorrectas en el formulario, luego la contraseña correcta: debe seguir fallando
por cuenta bloqueada. Desbloqueo con el endpoint existente y login correcto: debe funcionar y
reiniciar la cuenta.