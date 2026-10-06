# Generar el par de llaves RSA

Llaves que usa el Authorization Server para firmar y verificar los tokens JWT.

| Archivo | Tipo | Para qué |
|---------|------|----------|
| `private.pem` | Privada | Firma los tokens. Nunca sale del servidor. |
| `public.pem` | Pública | Verifica las firmas. Se publica en `/oauth2/jwks`. |

**Ubicación en el proyecto**

```
authorization-server/src/main/resources/keys/private.pem
authorization-server/src/main/resources/keys/public.pem
```

Los comandos son los mismos en todos los sistemas. Solo cambia cómo conseguir OpenSSL y desde qué terminal se ejecuta.

---

## macOS

OpenSSL (LibreSSL) ya viene instalado. Abre la app **Terminal**:

```bash
mkdir -p authorization-server/src/main/resources/keys
cd authorization-server/src/main/resources/keys

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -pubout -in private.pem -out public.pem
```

Opcional, si prefieres el OpenSSL de Homebrew: `brew install openssl`

---

## Ubuntu / Debian

Si no tienes OpenSSL:

```bash
sudo apt update
sudo apt install -y openssl
```

Luego:

```bash
mkdir -p authorization-server/src/main/resources/keys
cd authorization-server/src/main/resources/keys

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -pubout -in private.pem -out public.pem
```

---

## Windows

Elige **una** de estas opciones.

### Opción A: Git Bash (la más fácil)

Si tienes Git instalado, ya trae OpenSSL. Abre **Git Bash** y ejecuta los mismos comandos de macOS y Linux:

```bash
mkdir -p authorization-server/src/main/resources/keys
cd authorization-server/src/main/resources/keys

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -pubout -in private.pem -out public.pem
```

### Opción B: PowerShell

1. Instala OpenSSL:
   ```powershell
   winget install ShiningLight.OpenSSL.Light
   ```
2. Cierra y vuelve a abrir PowerShell.
3. Ejecuta:
   ```powershell
   mkdir authorization-server\src\main\resources\keys
   cd authorization-server\src\main\resources\keys

   openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
   openssl rsa -pubout -in private.pem -out public.pem
   ```

### Opción C: WSL

Abre la terminal de Ubuntu (WSL) y sigue los pasos de **Ubuntu / Debian**.

---

## Verificar

La primera línea de cada archivo debe ser exactamente:

| Archivo | Primera línea |
|---------|---------------|
| `private.pem` | `-----BEGIN PRIVATE KEY-----` |
| `public.pem` | `-----BEGIN PUBLIC KEY-----` |

```bash
# macOS / Linux / Git Bash
head -n 1 private.pem public.pem
```

```powershell
# PowerShell
Get-Content private.pem -TotalCount 1
```

---

## Problemas comunes

| Síntoma | Causa y solución |
|---------|------------------|
| La privada empieza con `-----BEGIN RSA PRIVATE KEY-----` | Es formato PKCS#1 y Spring espera PKCS#8. Conviértela: `openssl pkcs8 -topk8 -nocrypt -in private.pem -out private-pkcs8.pem` y renombra el resultado a `private.pem`. |
| `openssl: command not found` | OpenSSL no está instalado, o la terminal no se reinició. Instálalo y abre una terminal nueva. |
| `FileNotFoundException ... keys/private.pem` al arrancar | Los archivos no están en `src/main/resources/keys/`. Revisa la ruta y vuelve a compilar (`mvn compile`). |
| Saltos de línea distintos en Windows | Es normal. Spring lee ambos formatos. |

---

## Seguridad

- Estas llaves son **solo para el curso**. Se incluyen en el repositorio para que todos puedan clonar y ejecutar.
- **Nunca** subas una llave privada real a un repositorio. En producción va fuera del código (variable de entorno, volumen o gestor de secretos), y los `.pem` se agregan al `.gitignore`.
- Si una llave privada se filtra, se genera un par nuevo y se cambia el `keyID` (rotación de llaves).