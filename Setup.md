# Manual de bolsillo: instalación del entorno de desarrollo

Guía rápida para instalar en **Windows**, **macOS** y **Ubuntu/Debian**: Java JDK 25, Node.js (LTS), NPM, Maven 3.9.x, OpenSSL, Postman e IntelliJ IDEA.

---

## 1. Introducción: ¿qué necesito tener instalado?

| # | Herramienta | Versión requerida | Para qué sirve |
|---|-------------|-------------------|----------------|
| 1 | **Java JDK** | 25 | Compilar y ejecutar el backend en Java |
| 2 | **Node.js** | 18 o superior (se recomienda la LTS vigente) | Ejecutar el frontend y sus herramientas de build |
| 3 | **NPM** | Viene incluido con Node.js | Gestor de paquetes de JavaScript |
| 4 | **Maven** | 3.9.x | Gestión de dependencias y construcción del proyecto Java |
| 5 | **OpenSSL** | 3.x | Generar llaves, certificados y keystores |
| 6 | **Postman** | Última versión | Probar y documentar APIs |
| 7 | **IDE** | IntelliJ IDEA 2025.2.2 o superior (u otro IDE con soporte para Java 25) | Desarrollo y depuración |

**Orden recomendado de instalación:** JDK → Maven → Node.js/NPM → OpenSSL → Postman → IDE.

> **Importante:** Después de instalar cada herramienta, **cierra y vuelve a abrir la terminal** para que se carguen las variables de entorno (`PATH`, `JAVA_HOME`).

---

## 2. Windows 10/11

> Abre **PowerShell** (de preferencia como administrador). Los comandos `winget` vienen incluidos en Windows 10/11. Si no lo tienes, instálalo desde la Microsoft Store: <https://apps.microsoft.com/detail/9nblggh4nns1>

### 2.1 Java JDK 25

**Opción A: con winget (recomendada)**

```powershell
winget install EclipseAdoptium.Temurin.25.JDK
```

**Opción B: instalador manual**

- Eclipse Temurin: <https://adoptium.net/temurin/releases/?version=25>
- Oracle JDK: <https://www.oracle.com/java/technologies/downloads/#java25>
- Microsoft OpenJDK: <https://learn.microsoft.com/java/openjdk/download>

Descarga el `.msi`, ejecútalo y en el asistente activa **"Set JAVA_HOME variable"** y **"Add to PATH"**.

**Configurar `JAVA_HOME` manualmente (si hace falta):**

```powershell
# Ajusta la ruta a la que haya instalado tu JDK
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-25.0.0.0-hotspot" /M
```

Luego agrega `%JAVA_HOME%\bin` al `Path` (Panel de control → Sistema → Configuración avanzada → Variables de entorno).

**Verificar:**

```powershell
java -version
javac -version
```

### 2.2 Maven 3.9.x

**Opción A: con Chocolatey**

Instala Chocolatey: <https://chocolatey.org/install>. Después:

```powershell
choco install maven --version=3.9.11
```

> La versión exacta de la rama 3.9.x puede cambiar. Consulta la última en <https://maven.apache.org/download.cgi>.

**Opción B: instalación manual**

1. Descarga el ZIP binario desde <https://maven.apache.org/download.cgi> (`apache-maven-3.9.x-bin.zip`).
2. Descomprímelo en `C:\Tools\apache-maven-3.9.x`.
3. Configura las variables:

```powershell
setx MAVEN_HOME "C:\Tools\apache-maven-3.9.11" /M
setx PATH "$($env:PATH);C:\Tools\apache-maven-3.9.11\bin" /M
```

**Verificar:**

```powershell
mvn -version
```

### 2.3 Node.js (LTS) y NPM

**Opción A: con winget**

```powershell
winget install OpenJS.NodeJS.LTS
```

**Opción B: instalador manual**

Descarga el `.msi` de la versión **LTS** desde <https://nodejs.org/en/download>.

**Opción C: con nvm-windows (para manejar varias versiones)**

- <https://github.com/coreybutler/nvm-windows/releases>

```powershell
nvm install lts
nvm use lts
```

**Verificar** (NPM se instala junto con Node.js):

```powershell
node -v
npm -v
```

### 2.4 OpenSSL

**Opción A: con winget**

```powershell
winget install ShiningLight.OpenSSL.Light
```

**Opción B: instalador manual**

- <https://slproweb.com/products/Win32OpenSSL.html> (elige *Win64 OpenSSL v3.x Light*).

**Alternativa:** si ya tienes **Git for Windows** (<https://git-scm.com/download/win>), incluye OpenSSL en `C:\Program Files\Git\usr\bin\openssl.exe`.

Agrega la carpeta `bin` de OpenSSL al `PATH` si el comando no se reconoce.

**Verificar:**

```powershell
openssl version
```

### 2.5 Postman

```powershell
winget install Postman.Postman
```

O descarga el instalador desde <https://www.postman.com/downloads/>.

### 2.6 IntelliJ IDEA

```powershell
winget install JetBrains.IntelliJIDEA.Ultimate
```

O descarga el instalador desde <https://www.jetbrains.com/idea/download/>.

Alternativa para gestionar varios IDE de JetBrains: <https://www.jetbrains.com/toolbox-app/>

> **Soporte para Java 25:** requiere IntelliJ IDEA **2025.2.2 o superior**. Si ya tienes el IDE, actualízalo desde *Help → Check for Updates*.

**Otros IDE con soporte para Java 25:**

- Eclipse IDE: <https://www.eclipse.org/downloads/>
- Visual Studio Code + Extension Pack for Java: <https://code.visualstudio.com/download>
- Apache NetBeans: <https://netbeans.apache.org/front/main/download/>

---

## 3. macOS

> Se recomienda usar **Homebrew**. Instálalo con (<https://brew.sh>):
>
> ```bash
> /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
> ```

### 3.1 Java JDK 25

**Opción A: con Homebrew (recomendada)**

```bash
brew install --cask temurin@25
```

**Opción B: instalador manual**

- Eclipse Temurin: <https://adoptium.net/temurin/releases/?version=25>
- Oracle JDK: <https://www.oracle.com/java/technologies/downloads/#java25>

**Configurar `JAVA_HOME`** (agrega a `~/.zshrc`):

```bash
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 25)' >> ~/.zshrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

**Verificar:**

```bash
java -version
javac -version
```

### 3.2 Maven 3.9.x

```bash
brew install maven
```

> Homebrew instala la última versión estable. Si necesitas una 3.9.x exacta, descarga el binario desde <https://maven.apache.org/download.cgi>, descomprímelo en `/opt/` y agrega su `bin` al `PATH`:
>
> ```bash
> sudo tar -xzf apache-maven-3.9.11-bin.tar.gz -C /opt
> echo 'export PATH="/opt/apache-maven-3.9.11/bin:$PATH"' >> ~/.zshrc
> source ~/.zshrc
> ```

**Verificar:**

```bash
mvn -version
```

### 3.3 Node.js (LTS) y NPM

**Opción A: con Homebrew**

```bash
brew install node@24
```

> Revisa cuál es la versión LTS vigente en <https://nodejs.org/en/about/previous-releases> y ajusta el número.

**Opción B: instalador `.pkg`**

Descárgalo desde <https://nodejs.org/en/download>.

**Opción C: con nvm (recomendada para manejar varias versiones)**

- <https://github.com/nvm-sh/nvm>

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
source ~/.zshrc
nvm install --lts
nvm use --lts
```

**Verificar:**

```bash
node -v
npm -v
```

### 3.4 OpenSSL

macOS incluye *LibreSSL*. Para instalar OpenSSL 3:

```bash
brew install openssl@3
echo 'export PATH="$(brew --prefix openssl@3)/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

**Verificar:**

```bash
openssl version
```

Debe decir `OpenSSL 3.x`, no `LibreSSL`.

### 3.5 Postman

```bash
brew install --cask postman
```

O descarga el instalador desde <https://www.postman.com/downloads/>.

### 3.6 IntelliJ IDEA

```bash
brew install --cask intellij-idea
```

O descarga el `.dmg` desde <https://www.jetbrains.com/idea/download/> (elige la versión para Apple Silicon o Intel según tu Mac).

> Requiere **IntelliJ IDEA 2025.2.2 o superior** para Java 25.

---

## 4. Ubuntu / Debian

> Abre una terminal y actualiza los índices antes de comenzar:
>
> ```bash
> sudo apt update && sudo apt upgrade -y
> sudo apt install -y curl wget gpg apt-transport-https unzip
> ```

### 4.1 Java JDK 25

**Opción A: repositorio oficial de Eclipse Temurin (funciona en Ubuntu y Debian)**

Documentación: <https://adoptium.net/installation/linux/>

```bash
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update
sudo apt install -y temurin-25-jdk
```

**Opción B: paquete de la distribución** (solo si tu versión de Ubuntu/Debian ya lo incluye)

```bash
sudo apt install -y openjdk-25-jdk
```

**Opción C: con SDKMAN** (útil para cambiar entre versiones)

- <https://sdkman.io/install>

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk list java          # busca el identificador de la 25 (por ejemplo 25.x.x-tem)
sdk install java <identificador>
```

**Configurar `JAVA_HOME`** (agrega a `~/.bashrc`):

```bash
echo 'export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which javac))))' >> ~/.bashrc
source ~/.bashrc
```

**Verificar:**

```bash
java -version
javac -version
```

Si tienes varias versiones: `sudo update-alternatives --config java`

### 4.2 Maven 3.9.x

**Opción A: con apt** (la versión puede ser más antigua según la distribución)

```bash
sudo apt install -y maven
mvn -version
```

**Opción B: instalación manual de la 3.9.x (recomendada si apt trae una versión menor)**

Descargas: <https://maven.apache.org/download.cgi>

```bash
cd /tmp
wget https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz
sudo tar -xzf apache-maven-3.9.11-bin.tar.gz -C /opt
sudo ln -sfn /opt/apache-maven-3.9.11 /opt/maven
echo 'export M2_HOME=/opt/maven' >> ~/.bashrc
echo 'export PATH="$M2_HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

> Cambia `3.9.11` por la última 3.9.x publicada en la página de descargas.

**Verificar:**

```bash
mvn -version
```

### 4.3 Node.js (LTS) y NPM

**Opción A: con nvm (recomendada)**

- <https://github.com/nvm-sh/nvm>

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
source ~/.bashrc
nvm install --lts
nvm use --lts
```

**Opción B: repositorio NodeSource**

- <https://github.com/nodesource/distributions>

```bash
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
sudo apt install -y nodejs
```

> No uses `sudo apt install nodejs npm` sin configurar un repositorio: suele instalar una versión muy antigua.

**Verificar:**

```bash
node -v
npm -v
```

### 4.4 OpenSSL

```bash
sudo apt install -y openssl
```

**Verificar:**

```bash
openssl version
```

### 4.5 Postman

**Opción A: con Snap**

```bash
sudo snap install postman
```

**Opción B: tarball oficial**

```bash
cd /tmp
wget https://dl.pstmn.io/download/latest/linux_64 -O postman.tar.gz
sudo tar -xzf postman.tar.gz -C /opt
sudo ln -sf /opt/Postman/Postman /usr/local/bin/postman
```

Página oficial de descargas: <https://www.postman.com/downloads/>

### 4.6 IntelliJ IDEA

**Opción A: con JetBrains Toolbox (recomendada)**

- <https://www.jetbrains.com/toolbox-app/>

**Opción B: con Snap**

```bash
sudo snap install intellij-idea-community --classic
# o la edición Ultimate:
sudo snap install intellij-idea-ultimate --classic
```

**Opción C: tarball manual**

Descarga el `.tar.gz` desde <https://www.jetbrains.com/idea/download/#section=linux> y descomprímelo:

```bash
sudo tar -xzf ideaIU-*.tar.gz -C /opt
/opt/idea-*/bin/idea.sh
```

> Requiere **IntelliJ IDEA 2025.2.2 o superior** para Java 25.

---

## 5. Verificación final (todos los sistemas)

Abre una terminal nueva y ejecuta:

```bash
java -version
javac -version
mvn -version
node -v
npm -v
openssl version
```

**Resultado esperado:**

| Comando | Debe mostrar |
|---------|--------------|
| `java -version` | `openjdk version "25..."` (o Oracle/Temurin 25) |
| `mvn -version` | `Apache Maven 3.9.x` y `Java version: 25...` |
| `node -v` | `v18.x` o superior |
| `npm -v` | Cualquier versión (viene con Node) |
| `openssl version` | `OpenSSL 3.x` |

---

## 6. Problemas comunes

| Problema | Solución |
|----------|----------|
| `java` no se reconoce | Reabre la terminal y revisa `JAVA_HOME` y el `PATH` |
| `mvn` usa otra versión de Java | Verifica que `JAVA_HOME` apunte al JDK 25 (`mvn -version` muestra la ruta) |
| `node` muestra una versión vieja | Con nvm: `nvm use --lts`. Revisa que no haya otra instalación en el `PATH` |
| `openssl` no se reconoce en Windows | Agrega la carpeta `bin` de OpenSSL al `PATH` o usa el de Git for Windows |
| IntelliJ no reconoce Java 25 | Actualiza el IDE y en *File → Project Structure → SDKs* agrega el JDK 25 |
| Permisos denegados con npm en Linux/Mac | No uses `sudo npm`; instala Node con nvm |

---

## 7. Enlaces oficiales de referencia

- Eclipse Temurin (JDK): <https://adoptium.net/>
- Oracle JDK: <https://www.oracle.com/java/technologies/downloads/>
- Apache Maven: <https://maven.apache.org/download.cgi>
- Node.js: <https://nodejs.org/>
- NPM: <https://docs.npmjs.com/downloading-and-installing-node-js-and-npm>
- OpenSSL: <https://www.openssl.org/>
- Postman: <https://www.postman.com/downloads/>
- IntelliJ IDEA: <https://www.jetbrains.com/idea/download/>
- Homebrew (macOS): <https://brew.sh/>
- Winget (Windows): <https://learn.microsoft.com/windows/package-manager/winget/>
- SDKMAN: <https://sdkman.io/>
- nvm: <https://github.com/nvm-sh/nvm>