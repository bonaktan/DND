# ADR-0001 — Technical Architecture and Technology Stack

**Status:** Partially accepted
**Date:** November 26, 2026
**Scope:** Application architecture, development infrastructure, runtime execution, rendering, and resource management.
**Use of AI Declaration:** AI was used to create this ADR with guidance from the Technical Lead and to revise it against the repository.

## 1. Context

DND is being developed as a Java-based, two-dimensional desktop RPG with a retro graphical presentation. The current application is a libGDX desktop demo that opens a window and draws “Hello World!”.

The architecture prioritizes simplicity, maintainability, and reuse of libGDX infrastructure. As gameplay is added, keep runtime coordination, application state, rendering, and domain logic separate where that makes the code easier to maintain and test.

## 2. Technology Stack

| Technology | Role in the project | Status |
|---|---|---|
| Java | Primary programming language | Required |
| libGDX 1.14.2 | Application lifecycle and rendering | Accepted and configured |
| LWJGL3 | Desktop backend through libGDX, with LWJGL dependencies constrained to 3.4.3 | Configured |
| OpenGL | Graphics API used through the desktop backend | In use |
| Gradle 9.7.1 | Wrapper, build, dependency management, and application distribution | Configured |
| Git and GitHub | Version control and the configured `origin` remote | In use |
| JUnit Jupiter 6.0.1 | Automated tests run on the JUnit Platform | Configured |
| Java 25 LTS | Build toolchain and application runtime baseline | Configured |
| Tiled | Tile map authoring | Proposed |
| LibreSprite | Graphical asset creation | Proposed |

The repository was scaffolded with `gradle init`. The build uses Kotlin DSL and resolves application dependencies from Maven Central. The application build pins its Java toolchain to JDK 25, and CI also uses JDK 25. Gradle settings configure the Foojay resolver to provision the toolchain if needed. The installed application uses the Java 25 runtime baseline.

## 3. Application Architecture

DND runs as a single desktop application. The Gradle root project includes one application subproject, `app`, which contains both the desktop entry point and the application code. The entry point, lifecycle callbacks, drawing, and graphics resource ownership currently live in `org.AngelosAura.app.Main`.

Keep the single-application layout as the implementation baseline. Introduce packages and classes when gameplay gives them distinct responsibilities, such as simulation, rendering, input, or resources. Prefer composition and reuse existing libGDX abstractions.

## 4. Runtime Architecture

libGDX manages the runtime through `Lwjgl3Application` and calls the lifecycle methods implemented by `Main`, which extends `ApplicationAdapter`. The launcher opens a non-resizable 480 × 270 window with vertical sync enabled. Each `render()` callback draws the demo frame. The window size is a demo setting, and vertical sync leaves frame pacing to the display and backend.

A fixed-timestep gameplay loop remains proposed. The proposal uses an accumulator with 60 simulation updates per second, or about 16.67 ms per update, and a separate rendering target of 60 frames per second. It would retain input events until a simulation update consumes them and bound elapsed time and catch-up work after stalls. Render interpolation is deferred until needed.

## 5. Threading and State Management

The demo creates, uses, and disposes graphics resources in the libGDX lifecycle callbacks. For gameplay, the proposed model keeps authoritative state mutations, simulation, and rendering on the libGDX application thread. Any future background workers should hand results back to that thread before they are applied to application state.

## 6. Rendering Architecture

`Main.render()` clears the screen and draws text with libGDX's `SpriteBatch` and `BitmapFont`. The demo uses the batch's default projection.

The proposed gameplay renderer uses libGDX's `OrthogonalTiledMapRenderer`, `OrthographicCamera`, and `FitViewport`, with nearest-neighbor texture filtering and `Animation` and `TextureRegion` for sprite animation. The proposal calls for an explicit layer order and Y-axis sorting by ground-contact position where depth ordering is needed. Rendering should read authoritative gameplay state. A dedicated renderer can take ownership once the game has world rendering to coordinate.

## 7. Coordinate System

The demo draws its text at `(24, 150)` using the batch's default projection. For gameplay, the coordinate proposal calls for a Y-up world with a bottom-left origin, one logical pixel per world unit, floating-point entity positions, integer tile coordinates, and a tile renderer unit scale of 1.0. World, tile, and screen coordinates should have explicit conversions when those systems are introduced.

## 8. Asset Management

`Main.create()` creates a single `SpriteBatch` and a default `BitmapFont`, and sets the font scale to 2. The callbacks reuse these objects across frames and release them in `Main.dispose()`. The root `assets/` directory is empty and has no Gradle resource configuration.

The proposed gameplay asset strategy uses libGDX's `AssetManager` for shared assets and map-specific loading and unloading. The proposal favors texture and animation reuse, texture atlases where compatible, and explicit disposal. Resource loading should stay out of render and simulation loops. Advanced asset streaming is deferred pending a measured need.

## 9. Project Organization

```text
DND/
├── app/
│   ├── build.gradle.kts
│   ├── src/org/AngelosAura/app/Main.java
│   └── test/org/AngelosAura/app/MainTest.java
├── assets/
├── docs/
│   └── adr/
│       └── 0001-technical-architecture
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── README.md
├── run.sh
└── run.bat
```

The Java source sets are `app/src` and `app/test`. The Gradle application plugin uses `org.AngelosAura.app.Main` as its entry point and sets JVM flags for native access and the `jdk.internal.misc` export. The launch helpers are `run.sh` on Linux and `run.bat` on Windows. Their build command runs Gradle's `build` and `:app:installDist` tasks. Their run command launches the installed application from `app/build/install/app/bin/`.

## 10. Current Class Responsibilities

| Class | Responsibility |
|---|---|
| `Main` | Configures and launches LWJGL3, implements the libGDX lifecycle, and owns the demo's rendering resources |
| `MainTest` | Runs a JVM-only JUnit test that constructs `Main` and checks that `Main.test()` returns 2 |

Introduce dedicated classes for game timing, input, rendering, and assets as those responsibilities emerge.

## 11. Performance and Resource Management

Performance optimization will follow a measurement-driven approach. The demo already reuses its rendering resources across frames and disposes them on shutdown. As gameplay is added, keep blocking file operations and avoidable allocations out of frequent updates, then measure frame duration, update duration, and resource use before adding optimizations such as viewport culling or object pooling. Validate performance and compatibility on the lowest-specification supported development machine once the hardware target is defined.

## 12. Testing and Observability

Gradle runs tests from `app/test` with JUnit Jupiter on the JUnit Platform. The existing test exercises a constant-return method without starting the desktop backend. Future game logic should remain testable independently of graphical presentation where practical.

Runtime diagnostics are proposed for frames and updates per second, frame and simulation duration, heap usage, active object counts, and rendering statistics. Keep diagnostics separate from authoritative gameplay logic.

## 13. Alternatives and Deferred Technologies

| Technology or approach | Disposition |
|---|---|
| Custom Java AWT engine | Replaced by libGDX |
| JavaFX rendering | Not selected |
| External ECS framework | Deferred |
| Box2D physics engine | Deferred |
| Dependency injection framework | Not required |
| Microservice architecture | Not required |
| Multithreaded simulation | Deferred |
| Multithreaded rendering | Not required |
| Custom shader pipeline | Deferred |
| Advanced asset streaming | Deferred |
| Object pooling | Subject to profiling |
| Render interpolation | Subject to visual requirements |

Deferred technologies may be reconsidered if subsequent technical requirements justify their adoption.

## 14. Architectural Principles

1. **Separation of concerns.** Give each component a clearly defined responsibility.
2. **Composition over inheritance.** Prefer composable capabilities over extensive class hierarchies.
3. **Single source of truth.** Give authoritative application state a clear owner.
4. **Predictable execution order.** Define the order of simulation operations when gameplay is introduced.
5. **Resource reuse.** Share resources where practical and dispose of them when their owner shuts down.
6. **Framework reuse.** Use suitable libGDX abstractions.
7. **Incremental complexity.** Introduce abstractions and subsystems when a concrete requirement calls for them.
8. **Measurement-driven optimization.** Base performance changes on observable evidence.
9. **Maintainability.** Favor readable implementations.
10. **Testability.** Keep application logic independently verifiable wherever practical.

## 15. Consequences

The single subproject builds, tests, and packages the desktop application through one Gradle configuration. It currently owns both desktop initialization and application code. Using libGDX reduces the lifecycle and rendering code the project has to maintain and ties the application to supported libGDX backends.

If adopted, fixed-timestep simulation will require timing and input buffering. A dedicated world renderer will need an explicit draw order, and preloading shared assets will add startup work and increase memory use.

## 16. Decision Status

Java is required, and libGDX is the accepted game development framework. The current Gradle layout, LWJGL3 desktop launcher, rendering demo, and JUnit setup describe the repository baseline. The gameplay timing, rendering, coordinate, and asset-management proposals remain subject to review.

The game virtual resolution, tile and sprite dimensions, and camera behavior remain undecided. Hardware compatibility and gameplay performance still need validation.
