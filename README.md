# CarBrowser para Android Auto

Navegador web completo con aceleración de video optimizado para ver **YouTube** y cualquier sitio web directamente en la pantalla de **Android Auto**.

---

## ⚠️ ¿Por qué las apps de video no aparecen solas en Android Auto?

Google impone restricciones de seguridad muy estrictas (*Driver Distraction Guidelines*) en Android Auto:
1. **Google no permite navegadores web ni reproductores de video libres** en la Google Play Store para Android Auto.
2. Si instalas un archivo `.apk` manualmente (sideload), Android Auto **lo oculta de la pantalla del vehículo de forma predeterminada**.

Para que cualquier APK externa aparezca en la pantalla de tu coche, **debes seguir los pasos de configuración que se explican a continuación**.

---

## 📱 Paso Obligatorio: Activar "Fuentes Desconocidas" en Android Auto

Para que tu teléfono le permita a Android Auto mostrar aplicaciones no instaladas desde la Play Store:

1. En tu teléfono móvil, abre **Ajustes** y escribe en el buscador: `Android Auto`.
2. Entra en los ajustes de **Android Auto** y baja hasta el final de la pantalla hasta encontrar el apartado **"Versión"**.
3. Pulsa **10 veces seguidas** sobre el texto "Versión".
4. Aparecerá un aviso: *¿Permitir ajustes de desarrollo?* -> Pulsa en **Aceptar**.
5. Toca en el menú de los **tres puntos** (arriba a la derecha) y entra en **Ajustes de desarrollador**.
6. Busca la casilla **"Fuentes desconocidas"** y **actívala**.
7. En el apartado *Modo de aplicación*, selecciona **Versión de lanzamiento** (o *Desarrollador*).

---

## 🔒 Solución específica para Android 14 y Android 15

En **Android 14 y Android 15**, Google introdujo una capa de seguridad adicional que comprueba si la app fue instalada con el identificador de Google Play Store (`com.android.vending`). Si solo instalas el APK tocándolo desde tu gestor de archivos, es posible que el icono no se muestre en el coche.

### Cómo instalar el APK correctamente en Android 14/15:

* **Método 1 (Recomendado sin PC): KingInstaller / AAAD**
  1. Descarga e instala **KingInstaller** (herramienta de código abierto para Android Auto) desde su repositorio de GitHub.
  2. Abre KingInstaller, selecciona el archivo `CarBrowser.apk` que compilaste o descargaste.
  3. Pulsa en **Install as Google Play Store**.
  4. De esta forma, Android Auto lo reconocerá como una app oficial.

* **Método 2 (Vía PC con cable USB y ADB):**
  Conecta tu móvil al PC con depuración USB activada y ejecuta en la terminal:
  ```bash
  adb install -i com.android.vending CarBrowser.apk
  ```

---

## 🏗️ Cómo compilar este proyecto

Este repositorio contiene todo el código fuente listo para compilar en **Android Studio**:

1. Abre este directorio en **Android Studio** (Hedgehog, Iguana, Jellyfish o más reciente).
2. Deja que Gradle sincronice las dependencias (`androidx.car.app:app:1.4.0`, etc.).
3. Ve a **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. El archivo generado estará en: `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🧩 Alternativas Listas para Usar (Si no deseas compilar)

Si buscas una aplicación ya terminada, firmada y con soporte activo de la comunidad:
- **Fermata Auto** (Andrey Pavlenko): Navegador web completo con pestañas, reproducción de YouTube sin anuncios y soporte de TV/IPTV.
- **CarStream** (TheKiranKumar / Shmykelsa): Wrapper especializado de YouTube para Android Auto con controles táctiles integrados.
- **AAAD** (*Android Auto Apps Downloader*): Instalador móvil que permite instalar Fermata Auto y CarStream con 1 solo toque configurando los permisos automáticamente.
