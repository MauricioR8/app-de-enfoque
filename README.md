# App de Enfoque 🎯

Un **launcher minimalista de Android** inspirado en *Oasis Launcher* y *Minimalist Phone*, con todas las funciones "Premium" desbloqueadas y de uso libre. Construido con **Kotlin + Jetpack Compose**.

> Diseño ultra‑minimalista: fondo sólido, tipografías limpias en blanco/gris, tarjetas con bordes finos de 1px y sin sombras.

---

## ✨ Características

1. **Pantalla principal (Home)**
   - Reloj digital centrado enmarcado por un arco circular incompleto.
   - Fecha en texto pequeño.
   - Nombre del launcher (personalizable) e iconos de **teléfono** y **cámara**.
   - Rejilla de apps, accesos directos web y **carpetas**.

2. **Cajón de aplicaciones (App Drawer)**
   - Lista alfabética con **buscador** (lupa) e "Instaladas recientemente".
   - **Índice A‑Z** lateral para scroll rápido.
   - Engranaje de **Ajustes**.
   - Menú contextual (mantener pulsado): añadir a inicio, añadir a carpeta, info de la app, desinstalar.

3. **Iconos visibles en todas partes** (búsqueda, cajón, inicio y carpetas) con opción global de
   **Color original** ↔ **Blanco y Negro** (silueta monocroma con contraste adaptado).

4. **Sistema de carpetas**
   - Hasta **10 apps** por carpeta.
   - Icono con **imagen personalizada** de la galería, **recortada automáticamente** a un cuadrado del tamaño estándar.

5. **Accesos directos web**
   - Reconoce y guarda los accesos directos fijados (p. ej. *"Añadir a pantalla de inicio"* de Chrome) y los muestra como apps normales.

6. **Pantalla lateral "Oasis" (widgets modulares)** — todo desbloqueado, sin botones *"Obtener Pro"*:
   - **Por hacer** (checklist con tachado y borrar).
   - **Notas** con paginación `< n/total >` y vista a pantalla completa.
   - **Calendario** con eventos del día.
   - **Temporizador Pomodoro** (Pomodoro / Pausa corta / Pausa larga).
   - **Progreso del tiempo** (Año, Mes, Semana, Día).
   - **Mini‑juegos** de agilidad mental (**2048** jugable; Sudoku, Serpiente, Ladrillos y Trivia marcados como *próximamente*).
   - **Módulo personalizado** (tarjeta libre, p. ej. *"Ejercicio diario de ajedrez"*).
   - Botón **"+ Añadir widget"** tipo píldora.

7. **Fondos de pantalla**
   - **Negro puro** y **Blanco puro** + paleta de **10 tonos relajados** (pastel, oscuros y neutros).

---

## 🧱 Arquitectura

```
app/src/main/java/com/mauricior8/enfoque/
├── MainActivity.kt            # Navegación (pager Oasis/Home/Drawer) + overlays + intents
├── EnfoqueApp.kt              # Application + contenedor de dependencias
├── data/
│   ├── model/Models.kt        # Modelos (IconMode, HomeEntry, Folder, WidgetConfig, ...)
│   ├── PreferencesManager.kt  # DataStore: ajustes + layout + tablero (JSON)
│   ├── AppRepository.kt       # Apps instaladas + accesos directos
│   ├── WebShortcutStore.kt    # Persistencia de accesos directos web
│   ├── IconUtils.kt           # Conversión de iconos a monocromo
│   └── ImageStorage.kt        # Recorte/guardado de imágenes de carpeta
└── ui/
    ├── theme/                 # Color, Type, Theme (negro/blanco + 10 colores)
    ├── components/            # OasisCard, PillButton, IconCell, DrawableImage
    ├── home/                  # HomeScreen, ClockArc, FolderCell
    ├── drawer/                # AppDrawerScreen
    ├── oasis/                 # OasisScreen, WidgetCards, MiniGameWidget, NoteFullScreen
    ├── settings/              # SettingsScreen
    ├── folder/                # FolderEditorDialog
    └── EnfoqueViewModel.kt    # Estado y acciones
```

- **UI:** Jetpack Compose + Material 3
- **Persistencia:** DataStore Preferences (ajustes/layout/widgets en JSON con `kotlinx.serialization`) y `SharedPreferences` (accesos directos)
- **Imágenes:** Coil

---

## 🛠️ Cómo compilar

> El proyecto está pensado para abrirse en **Android Studio** (no se compila en este entorno por falta del SDK de Android).

1. Abre **Android Studio** (Hedgehog o superior).
2. `File > Open...` y selecciona la carpeta del proyecto.
3. Deja que Gradle sincronice (descargará el SDK/dependencias).
4. Conecta un teléfono Android (**API 26+**) con depuración USB, o usa un emulador.
5. Pulsa **Run ▶**.

### Versiones
- Android Gradle Plugin **8.5.2**, Kotlin **1.9.24**, Compose Compiler **1.5.14**
- `compileSdk` / `targetSdk` **34**, `minSdk` **26**
- Gradle Wrapper **8.9**

---

## 📲 Establecer como launcher

Tras instalar, ve a **Ajustes** (engranaje del cajón) → **Establecer como pantalla de inicio**, o el sistema te preguntará qué launcher usar al pulsar el botón Inicio. Selecciona **App de Enfoque**.

> Para que el cajón liste todas las apps se usa el permiso `QUERY_ALL_PACKAGES`.

---

## 🚧 Notas y siguientes pasos
- Mini‑juegos pendientes (Sudoku, Serpiente, Ladrillos, Trivia): actualmente muestran un marcador *"próximamente"*; **2048** ya es jugable.
- El widget de "Uso de la App" del launcher original no está incluido todavía (requiere permiso de estadísticas de uso).
- Reordenar widgets/iconos por arrastre puede añadirse en una iteración futura.
