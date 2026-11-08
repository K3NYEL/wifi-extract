# Mobile Network Analyzer (MNA)

Aplicación nativa para Android de análisis de red y diagnóstico de conectividad, desarrollada con Kotlin y Jetpack Compose.

## Características principales

- **Inicio**: Panel general y accesos directos rápidos al estado de la red y herramientas.
- **Red**: Información detallada de la conexión activa, red local y escaneo de dispositivos y redes Wi-Fi cercanas.
- **Herramientas**: Diagnóstico de conectividad con pruebas de Ping, DNS y puertos TCP.
- **Ajustes**: Configuración local de la aplicación, comprobación de actualizaciones oficiales, directrices de privacidad y alcance de uso.

## Requisitos del sistema

- **Android Mínimo (minSdk)**: 26 (Android 8.0)
- **Android Objetivo (targetSdk / compileSdk)**: 37
- **JDK**: 17

## Compilación y Ejecución

1. Abre la raíz del repositorio en Android Studio.
2. Sincroniza el proyecto con Gradle.
3. Ejecuta la configuración de la aplicación (`:app`) en un dispositivo físico o emulador Android.

Para compilar un APK de depuración desde la terminal:

```bash
./gradlew :app:assembleDebug
```

El APK resultante se ubicará en `app/build/outputs/apk/debug/app-debug.apk`.

## Seguridad y Privacidad

Mobile Network Analyzer está diseñada exclusivamente para redes de propiedad del usuario o sobre las cuales se posea autorización explícita de análisis. 

Toda la información de red se procesa localmente en el dispositivo y no se transmite a servidores externos. Para más detalles, consulta [PRIVACY.md](PRIVACY.md).

## Documentación y Normas

- [Estándares del Proyecto](docs/PROJECT_STANDARDS.md)
- [Historial de Cambios](CHANGELOG.md)
- [Política de Seguridad](SECURITY.md)
