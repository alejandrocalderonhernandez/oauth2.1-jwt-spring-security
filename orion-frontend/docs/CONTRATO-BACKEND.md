# Orión · Guía técnica para construir los backends

Este documento describe lo que el frontend espera de tus servidores. El frontend es de práctica para el curso de **OAuth 2.1 con Spring**: una app de JavaScript puro (sin frameworks, sin build) que ya sabe hacer el login con **Authorization Code + PKCE**. Tú construyes los dos backends en clase: el **Authorization Server** y el **Resource Server**.

La historia: una base espacial con expedientes de misiones que nadie debería ver sin permiso (hay cosas alienígenas ahí dentro 👽).

> Para instalar Node o Python y levantar el frontend, mira el [README principal](../README.md).

## Los tres modos

Se cambian con `?mode=` en la URL o con `DEFAULT_MODE` en `js/config.js`.

| Modo | URL | Para qué sirve |
|------|-----|----------------|
| `mock` | `/?mode=mock` | No necesita backend. Los datos salen de `mock/*.json` y un selector arriba simula **Visitante / User / Admin**. Ideal para ensayar. |
| `open` | `/?mode=open` | Llama al resource server **real, sin login ni token**. Úsalo el primer día, mientras muestras las rutas en Postman. |
| `secure` | `/?mode=secure` | Flujo completo OAuth 2.1 con PKCE. Cuando el authorization server ya exista. |

## Las pantallas y las rutas

| Pantalla | Ruta del backend | Quién entra | Scope |
|----------|------------------|-------------|-------|
| Noticias | `GET /api/news` | Cualquiera, sin login | ninguno |
| Misiones | `GET /api/missions` | Usuarios con login | `mission.read` |
| Nueva misión | `POST /api/missions` | Solo admin | `mission.write` |

`GET` y `POST` comparten la ruta pero piden permisos distintos: en Spring el permiso depende también del **método HTTP**.

El formulario de "Nueva misión" es de mentira: sí hace el `POST`, pero el servidor responde siempre lo mismo y no guarda nada. Lo importante es el permiso.

### Usuarios y roles sugeridos

| Usuario | Rol | Scopes que debe poder obtener |
|---------|-----|-------------------------------|
| (visitante, sin login) | — | ninguno: solo Noticias |
| `user` | `USER` | `mission.read` |
| `admin` | `ADMIN` | `mission.read`, `mission.write` |

Resultados esperados en la demo:

- Visitante abre Misiones → **401**
- `user` abre Misiones → ✅ 200
- `user` envía una misión → **403** (tiene sesión, pero no el permiso)
- `admin` envía una misión → ✅ **201**

## Qué espera el frontend de tus servidores

### Authorization Server

- Un cliente registrado así:
  - `client_id`: `orion-frontend`
  - Cliente **público** (sin secreto, método de autenticación `none`)
  - Grant types: `authorization_code` y `refresh_token`
  - **PKCE obligatorio** (`S256`)
  - Redirect URI exacta: `http://localhost:5173/`
  - Scopes: `mission.read`, `mission.write`
- Rutas por defecto de Spring Authorization Server: `/oauth2/authorize` y `/oauth2/token`. Si publica `/.well-known/oauth-authorization-server`, la app lo usa (fase 0); si no, usa las rutas de `js/config.js`.
- **CORS** habilitado para `http://localhost:5173` en `/oauth2/token` y en `/.well-known/oauth-authorization-server`. La app llama a `/token` desde el navegador, así que sin CORS el canje falla.
- (Opcional) un claim `roles` en el access token, para que el chip de usuario muestre el rol. El nombre del claim se cambia en `config.js` (`roleClaim`).
- (Opcional) rotación de refresh tokens: la app guarda el nuevo y descarta el anterior.

### Resource Server

- Debe devolver exactamente el JSON de la carpeta `mock/`:
  - `GET /api/news` → `200` con `mock/news.json`
  - `GET /api/missions` → `200` con `mock/missions.json`
  - `POST /api/missions` → `201` con `mock/created.json` (el body que reciba se ignora)
- Sin token válido → **401**. Con token válido pero sin el scope → **403**.
- **CORS** para `http://localhost:5173`: métodos `GET, POST, OPTIONS` y headers `Authorization, Content-Type`. El `POST` con JSON dispara un preflight `OPTIONS` que debe responderse sin pedir token.
- Como es una API sin sesiones, desactiva CSRF; si no, el `POST` con Bearer devuelve 403 aunque el token sea correcto.
- En Spring los scopes llegan como autoridades `SCOPE_mission.read` y `SCOPE_mission.write`.

## El panel lateral (para explicar en clase)

El botón de arriba a la derecha abre el panel con tres pestañas:

- **Flujo**: las 8 fases del diagrama con los datos reales de cada una (state, code_challenge, el POST a `/token`…). Las fases se marcan en vivo: pendiente, en curso, listo, error, en el servidor u omitida.
- **Peticiones**: cada llamada a la API con sus headers, el `Authorization: Bearer …` y la respuesta.
- **Token**: el access token por dentro (si es JWT), sus scopes y cuánto falta para que caduque.

El **modo paso a paso** (activado por defecto) detiene la app antes de salir al login (fase 2) y antes de canjear el code (fase 5), para explicar qué está a punto de enviar. Se apaga desde el panel.

## Estructura

```
index.html          Una sola página (rutas con #/news, #/missions, #/missions/new)
serve.mjs           Servidor estático sin dependencias
css/styles.css      Tema Nord oscuro con naranja de contraste
js/config.js        ← lo único que normalmente tocas
js/pkce.js          code_verifier y code_challenge (Web Crypto)
js/oauth.js         Fases 0 a 5 y 7: discovery, login, callback, token, refresh
js/api.js           Llamadas a la API (Bearer, renovación, historial)
js/flow.js          Bitácora de fases que alimenta el panel
js/panel.js         Panel lateral
js/app.js           Pantallas y navegación
js/mock.js          Servidor simulado del modo mock
js/session.js       "¿Quién soy?": usuario, roles y scopes
js/modal.js         Ventana del modo paso a paso
js/icons.js         Íconos de Lucide incrustados (sin internet)
mock/               Datos estáticos: el contrato que debe devolver tu API
```

## Ojo con la seguridad (para comentar con los alumnos)

- Los tokens se guardan en `sessionStorage` solo para que la demo sobreviva a una recarga. En un proyecto real, una SPA que guarda tokens en el navegador queda expuesta si hay XSS. Lo más seguro es un backend que guarde los tokens y entregue al navegador solo una cookie de sesión (patrón BFF).
- Que la app lea el contenido del JWT es solo didáctico: los permisos reales los valida siempre el resource server.
- Aquí PKCE está escrito a mano para que se vea. En un proyecto real conviene una librería como `oidc-client-ts`.
- Todos los textos de la API se escapan antes de pintarlos en pantalla.
