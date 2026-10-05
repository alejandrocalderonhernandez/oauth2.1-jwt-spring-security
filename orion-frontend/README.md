# 🛰️ Orión · Control de misión

Frontend de práctica para el curso de OAuth 2.1. Es una página web de JavaScript puro: **no hay que instalar dependencias ni compilar nada**. Solo necesitas un servidor local muy pequeño, y para eso basta con **Node.js o Python** (elige uno).

---

## En 30 segundos

Si ya tienes Node o Python, esto es todo:

```bash
cd orion-frontend
node serve.mjs                    # con Node
python3 -m http.server 5173       # o con Python (en Windows: python -m http.server 5173)
```

Y abre 👉 **<http://localhost:5173>**

Si no los tienes, sigue los pasos de abajo.

---

## Paso 1 · Instala Node.js **o** Python

Necesitas **solo uno**. Node es la opción recomendada porque el proyecto trae su propio servidor (`serve.mjs`).

### 🪟 Windows

**Node.js** (recomendado)
1. Entra a <https://nodejs.org> y descarga la versión **LTS**.
2. Ejecuta el instalador y deja todo por defecto (Next, Next, Install).

O desde una terminal (PowerShell):
```powershell
winget install OpenJS.NodeJS.LTS
```

**Python**
1. Entra a <https://www.python.org/downloads/> y descarga la última versión.
2. En el instalador, **marca la casilla "Add python.exe to PATH"** antes de instalar. Es el error más común.

O desde PowerShell:
```powershell
winget install Python.Python.3.12
```

### 🍎 macOS

**Node.js** (recomendado)
- Descarga el instalador **LTS** desde <https://nodejs.org> y ábrelo, o bien, si usas Homebrew:
```bash
brew install node
```

**Python**
- Normalmente ya viene instalado. Compruébalo con `python3 --version`. Si no está:
```bash
brew install python
```
- O descarga el instalador desde <https://www.python.org/downloads/>.

> ¿No tienes Homebrew? Instálalo desde <https://brew.sh>. Es opcional: los instaladores de nodejs.org y python.org funcionan igual.

### 🐧 Linux

Elige el comando según tu distribución.

**Node.js** (recomendado)
```bash
# Ubuntu / Debian / Mint
sudo apt update && sudo apt install nodejs

# Fedora
sudo dnf install nodejs

# Arch / Manjaro
sudo pacman -S nodejs
```
> En distribuciones antiguas, `apt` puede instalar una versión vieja de Node. Si `node --version` muestra menos de 18, instala una versión actual con [nvm](https://github.com/nvm-sh/nvm) o desde <https://nodejs.org>.

**Python**
```bash
# Ubuntu / Debian / Mint
sudo apt update && sudo apt install python3

# Fedora
sudo dnf install python3

# Arch / Manjaro
sudo pacman -S python
```
La mayoría de las distribuciones ya lo traen preinstalado.

### ✅ Comprueba que quedó bien

Abre una terminal **nueva** (cierra la que tenías abierta) y ejecuta uno de estos:

| Programa | Windows | macOS / Linux | Debe mostrar |
|----------|---------|---------------|--------------|
| Node | `node --version` | `node --version` | `v18` o superior |
| Python | `python --version` | `python3 --version` | `Python 3.x` |

---

## Paso 2 · Descomprime y abre una terminal en la carpeta

1. Descomprime `orion-frontend.zip`. Te queda una carpeta `orion-frontend`.
2. Abre una terminal **dentro** de esa carpeta:

| Sistema | Cómo |
|---------|------|
| 🪟 **Windows** | Abre la carpeta en el Explorador, haz clic en la barra de direcciones, escribe `powershell` y pulsa Enter. (O clic derecho dentro de la carpeta → *Abrir en Terminal*). |
| 🍎 **macOS** | Abre *Terminal*, escribe `cd ` (con un espacio al final), **arrastra la carpeta** a la ventana y pulsa Enter. |
| 🐧 **Linux** | Clic derecho dentro de la carpeta → *Abrir en terminal*. O `cd` hasta ella. |

Para saber si estás en el lugar correcto, el comando `ls` (macOS/Linux) o `dir` (Windows) debe mostrar `index.html`, `serve.mjs`, `js`, `css`…

> **Consejo con espacios en la ruta:** si tu ruta tiene espacios, ponla entre comillas. En macOS/Linux, usa `~` **fuera** de las comillas:
> ```bash
> cd ~/"Pictures/Debuggeando Ideas/orion-frontend"
> ```

---

## Paso 3 · Levanta el frontend

Con **Node**:
```bash
node serve.mjs
```

Con **Python**:
```bash
python3 -m http.server 5173      # macOS / Linux
python -m http.server 5173       # Windows
```

Verás un mensaje como `Orión listo en http://localhost:5173` (Node) o `Serving HTTP on ... port 5173` (Python). **Deja esa terminal abierta**: mientras esté abierta, el servidor está funcionando.

## Paso 4 · Ábrelo en el navegador

👉 **<http://localhost:5173>**

Para detenerlo, vuelve a la terminal y pulsa `Ctrl + C`.

---

## ¿Qué vas a ver?

El proyecto arranca en **modo mock**: funciona sin ningún backend y tiene un simulador arriba a la derecha (**Simular:**) para fingir un **Visitante**, un **User** o un **Admin**. Ese simulador solo existe en modo mock; en el modo seguro el rol lo decide el servidor. Con él ya puedes recorrer las tres pantallas y ver cómo cambian los permisos.

De fondo hay un cielo con estrellas, ovnis y marcianos flotando. Es solo decoración; si tu sistema tiene activado "reducir movimiento", se queda quieto.

Cuando tengas tus servidores de Spring, cambia de modo con la URL:

| Modo | URL | Qué hace |
|------|-----|----------|
| 🧪 Mock | <http://localhost:5173/?mode=mock> | Datos de ejemplo, sin backend. |
| 🔓 Abierto | <http://localhost:5173/?mode=open> | Llama a tu API en `localhost:8080` **sin login**. Para mostrar las rutas en Postman. |
| 🔐 Seguro | <http://localhost:5173/?mode=secure> | Login completo con OAuth 2.1 y PKCE (servidor de autorización en `localhost:9000`). |

También puedes cambiar de modo **sin tocar la URL**: abre el panel lateral (botón de arriba a la derecha) y, en la pestaña **Flujo**, elige **Mock · Abierto · Seguro**. La app se recarga en el modo elegido.

Los puertos de tus servidores y el nombre del cliente se cambian en el archivo `js/config.js`.

---

## 🛠️ Problemas comunes

**`node` no se reconoce como comando / `command not found`**
Cierra la terminal y abre una nueva después de instalar. Si sigue igual, reinstala y asegúrate de que se agregó al PATH (en Python de Windows: la casilla *Add to PATH*).

**`Address already in use` / el puerto 5173 está ocupado**
Hay otro programa usando ese puerto (o ya tienes el servidor abierto en otra terminal). Ciérralo, o usa otro puerto: `node serve.mjs 3000`. Ojo: si cambias el puerto, también hay que cambiar la *redirect URI* registrada en tu servidor de autorización, que debe coincidir exactamente.

**La página sale en blanco o sin estilos**
Casi siempre es que abriste `index.html` con doble clic (`file://...`). Tiene que abrirse desde `http://localhost:5173`, con el servidor corriendo.

**Falla el login con un error sobre `crypto.subtle`**
Entra con `localhost`, no con una IP como `127.0.0.1` o `192.168.x.x`. PKCE necesita una función del navegador que solo existe en `localhost` o con `https`.

**Al canjear el code sale un error de CORS**
Tu authorization server (`/oauth2/token`) y tu resource server deben permitir peticiones desde `http://localhost:5173`. Los detalles están en la guía técnica.

---

## 📚 ¿Y ahora qué?

- Qué rutas, scopes y respuestas deben tener tus backends: [`docs/CONTRATO-BACKEND.md`](docs/CONTRATO-BACKEND.md)
- Los datos de ejemplo que debe devolver tu API: carpeta [`mock/`](mock/)
- Para explicar el flujo en clase, abre el **panel lateral** (botón arriba a la derecha): muestra las 8 fases del diagrama con datos reales.
