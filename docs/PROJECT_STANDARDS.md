# Mobile Network Analyzer — Normas del proyecto

Este documento es normativo. Las decisiones de arquitectura, diseño, seguridad, publicación y contribución deben cumplirlo. Si una limitación de Android impide una función, la aplicación debe explicarlo en lugar de simular que funciona.

## 1. Seguridad

### Permisos y privacidad
- Solicitar únicamente `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` e `INTERNET` como permisos base.
- Cualquier permiso adicional se solicita en el momento de utilizar la función, precedido de una explicación clara.
- No recopilar ni enviar a servidores propios datos de red, direcciones IP, MAC o nombres de dispositivos.
- No incorporar analítica ni publicidad identificativa. Cualquier telemetría futura debe ser opcional, anónima y desactivada por defecto.
- Guardar localmente solo lo imprescindible. Desactivar las copias de seguridad de datos sensibles y definir reglas explícitas si se habilitan copias en el futuro.

### Código y comunicaciones
- Usar HTTPS para toda comunicación remota. La configuración de seguridad de red debe bloquear el tráfico en claro.
- Validar y sanear entradas de usuario (IP, host y puerto), con límites de longitud y rango.
- No construir comandos de shell con entradas del usuario ni ejecutar comandos arbitrarios.
- Habilitar R8/ProGuard en variantes release. No registrar datos de red en logs de producción.
- Mantener dependencias revisadas y actualizadas; habilitar Dependabot.

### Actualizaciones
- Descargar paquetes únicamente desde GitHub Releases oficial del proyecto.
- Verificar SHA-256 publicado y certificado/firma del APK antes de ofrecer la instalación.
- Mostrar versión, origen y resultado de las verificaciones.
- Solicitar confirmación explícita antes de abrir el flujo de instalación de Android. Nunca instalar en silencio.
- Si no puede verificarse el hash o la firma, cancelar la actualización y explicar el motivo.

### Firma y secretos
- Keystore y contraseñas solo en GitHub Secrets; nunca en el repositorio ni en artefactos de diagnóstico.
- Workflows con permisos mínimos y tokens de alcance limitado.
- Ejecutar escaneo de secretos (Gitleaks o equivalente) en CI y antes de publicar.
- No imprimir secretos ni datos sensibles en logs.

## 2. UX/UI

### Claridad y honestidad
- Todo control activo ejecuta una acción real. Las funciones no implementadas se muestran como «Próximamente» y no son pulsables.
- Cada pantalla presenta estados de carga, vacío, éxito y error según corresponda.
- Los errores explican qué ocurrió y qué puede hacer la persona.
- Los trabajos prolongados muestran progreso y permiten cancelación.

### Usabilidad
- Navegación inferior con un máximo de cinco destinos, siempre visible y coherente.
- Objetivos táctiles de al menos 48 dp.
- Acciones principales al alcance del pulgar.
- Datos técnicos (IP, gateway y resultados) copiables con una pulsación.
- Lenguaje claro y ayuda breve para los términos técnicos.

### Accesibilidad
- Contraste mínimo 4.5:1 para texto normal y 3:1 para texto grande.
- No comunicar estados solo mediante color: acompañar con texto o icono.
- Todos los iconos informativos y controles tienen descripciones accesibles.
- Compatibilidad con TalkBack y escalado de fuente del sistema.
- Respetar la preferencia del sistema para reducir animaciones.

### Plataforma
- Material 3 como base, botón Atrás y gestos nativos de Android.
- Adaptación a rotación, pantallas pequeñas, plegables y distintas densidades.
- Localización desde el inicio mediante recursos Android: español e inglés como mínimo.

## 3. Infraestructura

### Arquitectura objetivo
Separar responsabilidades en módulos Gradle:
- `core`: utilidades, modelos y contratos compartidos.
- `design-system`: tokens, tipografía y componentes visuales reutilizables.
- `overview`: resumen de conexión.
- `discovery`: descubrimiento local autorizado.
- `diagnostics`: diagnósticos de red.
- `updater`: consulta y verificación de releases.
- `settings`: preferencias, privacidad y licencias.
- `app`: composición y navegación.

Aplicar MVVM/UDF con ViewModel, StateFlow y repositorios. La UI no debe consultar directamente las APIs de red. Usar corrutinas con cancelación, timeouts y concurrencia limitada. Adoptar Hilt o Koin cuando se introduzca la composición modular y probar los límites de cada módulo.

### Calidad
- Pruebas unitarias para cálculo de subred, parseo y validación.
- Pruebas Compose para flujos críticos.
- ktlint, detekt y Android Lint sin errores.
- Pull requests revisadas y rama `main` protegida.
- No integrar ni publicar cambios sin validación de CI.

### CI/CD
- En cada PR: compilación, Android Lint, análisis estático y pruebas.
- En cada tag semántico: generar APK/AAB firmado, calcular SHA-256 y publicar release con changelog.
- Versionado MAJOR.MINOR.PATCH y `versionCode` incremental.
- Cachear Gradle y fijar las acciones de GitHub a versiones inmutables (SHA revisado).
- La firma release depende de secretos configurados; nunca usar claves de prueba como firma de distribución.

### Compatibilidad y rendimiento
- Definir explícitamente minSdk y targetSdk; actualizar targetSdk según los requisitos de Google Play.
- Medir batería y tráfico. Los escaneos deben ser acotados, cancelables y no ejecutarse en segundo plano sin motivo; respetar Doze.
- Registrar fallos localmente. El usuario decide si comparte el informe; no se envía automáticamente.

## 4. Armonía visual

### Paleta
| Token | Valor | Uso |
|---|---|---|
| Background | `#0A0A0B` | Fondo principal |
| Surface | `#17171A` | Superficie base |
| Surface elevated | `#202024` | Superficie secundaria |
| Border | `#2C2C31` | Bordes |
| Accent | `#E5322D` | Acción principal y selección |
| Success | Verde semántico | Solo éxito, siempre con texto/icono |
| Warning | Ámbar semántico | Solo avisos, siempre con texto/icono |
| Error | Rojo semántico | Solo error, siempre con texto/icono |

Un único acento visual. No añadir colores decorativos.

### Tipografía
- Inter para interfaz y JetBrains Mono para valores técnicos.
- Escala fija: 11, 13, 14, 18, 22 y 28 sp.
- Etiquetas pequeñas en mayúsculas y con espaciado; valores técnicos monoespaciados.
- Si las fuentes no están empaquetadas, definir una estrategia de fallback coherente y con licencias verificadas.

### Forma y espacio
- Tarjetas con esquinas de 16 dp; botones con esquinas de 12 dp; chips con forma Pill totalmente redondeada.
- Rejilla de 4/8 dp para márgenes, rellenos y separaciones.
- No usar bordes grises para separar filas; expresar la jerarquía mediante espacio y superficies. Sin sombras pesadas.

### Iconografía y componentes
- Un solo conjunto de iconos, con trazo y rejilla coherentes de 24 dp.
- Botón primario rojo, secundario grafito y botón de texto.
- Un único botón primario por pantalla.
- Centralizar tokens y componentes en `design-system`; no hardcodear estilos en pantallas.

### Movimiento
- Animaciones funcionales de 150–250 ms.
- Sin movimiento decorativo y respetando la opción de reducir animaciones del sistema.

## 5. Ética y uso responsable

- Analizar únicamente la red a la que el usuario está conectado y solo redes propias o autorizadas.
- Mostrar el aviso: «Usa esta herramienta solo en redes que te pertenezcan o donde tengas permiso».
- No implementar ataques, suplantación, desautenticación, fuerza bruta ni recuperación de contraseñas.
- Limitar ritmo, concurrencia y cantidad de puertos para evitar perjudicar redes o dispositivos.
- Política de privacidad breve, legible y en lenguaje claro.
- Explicar qué datos se leen, para qué y que permanecen en el dispositivo.
- Si se distribuye en Google Play, cumplir RGPD y la política de datos de Google Play aplicables.
- Identificar honestamente funciones en desarrollo; nunca presentar simulaciones como resultados reales.

### Licencia y comunidad
- El proyecto debe elegir y publicar una licencia explícita (MIT, Apache-2.0 o GPL) antes de la primera distribución pública estable. La elección queda pendiente de aprobación del propietario; no se asumirá una licencia por defecto.
- Respetar licencias de dependencias y añadir una pantalla de licencias en Ajustes.
- Mantener `CHANGELOG.md` y `SECURITY.md`.
- Lenguaje neutral, accesible y sin cuentas o servicios propietarios obligatorios.

### Sostenibilidad
- Mantener la aplicación ligera.
- Evitar servicios persistentes o trabajo en segundo plano innecesario.
- Reducir consumo de batería y datos; permitir detener operaciones activas.
