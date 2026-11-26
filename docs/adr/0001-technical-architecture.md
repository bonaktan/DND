# ADR-0001 — Technical Architecture and Technology Stack

**Status:** Partially accepted
**Date:** November 26, 2026
**Scope:** Application architecture, development infrastructure, runtime execution, rendering, and game content.
**Use of AI Declaration:** AI was used to create this ADR with guidance from the Technical Lead and to revise it against the repository.

## 1. Context

DND is being developed as a Java RPG. The current entry point is a command-line stub that prints “Hello World!”.

The game needs dungeon generation, inventory, shortest paths, enemy prioritization, and save/load. Asset designers and gameplay balancers must be able to change art, text, encounters, and game balance without editing Java or recompiling. [Core gameplay requirements](../01-core-gameplay.md)

## 2. Technology Stack

| Technology | Role in the project | Status |
|---|---|---|
| Java | Primary programming language | Required |
| libGDX | Desktop GUI and rendering framework | Accepted |
| LWJGL3 | Desktop backend for libGDX | Accepted |
| OpenGL | Graphics API used by the desktop backend | Accepted |
| Gradle 9.7.1 | Wrapper, build, dependency management, and application distribution | Configured |
| Git and GitHub | Version control and the configured `origin` remote | In use |
| JUnit Jupiter 6.0.1 | Automated tests run on the JUnit Platform | Configured |
| Java 25 LTS | Build toolchain and application runtime baseline | Configured |
| Tiled | Tile map authoring | Proposed |
| LibreSprite | Graphical asset creation | Proposed |

The repository was scaffolded with `gradle init`. The build uses Kotlin DSL and resolves application dependencies from Maven Central. The application build pins its Java toolchain to JDK 25, and CI also uses JDK 25. Gradle settings configure the Foojay resolver to provision the toolchain if needed. The installed application uses the Java 25 runtime baseline.

## 3. Application Architecture

DND currently has one Gradle subproject, `app`. Keep one subproject and give each part of the game its own Java package as it is introduced.

| Package | Responsibility |
|---|---|
| `runtime` | Handles application startup, input, update timing, and starting or restoring a game session. |
| `game` | Owns the game state and rules. Contains dungeon generation, movement and pathfinding, enemy prioritization, combat, and inventory. Runs in plain Java without a window or file access. |
| `presentation` | Draws the world, menus, and player status. Reads gameplay state and sends player actions through the runtime. |
| `content` | Loads and validates authored files and converts them into typed, read-only definitions. Gameplay receives Java values such as enemy stats and map layouts. Presentation resolves sprite and sound IDs. |
| `save` | Reads and writes versioned snapshots of player progress in a writable user directory. The game session owns the state after it is restored. |

In the proposed design, the runtime loads the content and passes the definitions to a new game session. During play, it turns input into commands such as move or attack. Gameplay decides whether a command is valid and is the only part that changes game state. Presentation reads and displays the result. The course algorithms can be tested directly with JUnit.

## 4. Runtime Architecture

Use a fixed timestep for gameplay simulation. Each update advances the game by the same amount of simulation time, and the interface can refresh independently. Queue input until the next update can consume it, and limit catch-up work after stalls. The update rate is still open.

## 5. Threading and State Management

Keep gameplay updates on one thread. If background workers are introduced later, pass their results back to the game session before applying any change to game state.

## 6. Rendering Architecture

Build the desktop GUI with libGDX. Use its LWJGL3 backend to open the window and render through OpenGL. Keep drawing in the presentation layer, use nearest-neighbor scaling for pixel art, and define the drawing order for map layers and sprites.

## 7. Coordinate System

Use integer tile coordinates for the dungeon grid and pathfinding. Keep conversions from game coordinates to screen coordinates in the presentation layer.

## 8. Game Content and Assets

Keep designer-controlled values and player-facing strings under `assets/`. JSON definitions hold player starting stats, enemy stats, abilities, items, dungeon settings, and encounter weights. A text catalog holds names, dialogue, and UI labels. Art and audio are referenced by stable IDs, so an asset can change without updating every enemy or item that uses it.

Java defines what a rule does. For example, a balancer can create a new ability from supported damage and poison effects and set the poison duration in data. Adding an effect that rewinds a turn requires a new rule in Java. For the first release, support the behaviors and effects needed by the game's actual encounters.

An enemy file such as `assets/data/enemies/crypt-golem.json` could look like this.

```json
{
  "id": "crypt-golem",
  "nameKey": "enemy.crypt_golem.name",
  "spriteId": "crypt-golem",
  "maxHp": 36,
  "behavior": "pursue",
  "abilities": ["slam"]
}
```

A balancer can copy that file, give the copy a new ID, tune its stats, and add it to a dungeon's encounter pool. An artist can replace the image or update the sprite mapping. Renaming the golem only requires editing its text entry. Keep existing IDs stable when changing a display name, stat, or filename.

## 9. Project Organization

Keep the existing `app/src` and `app/test` source sets and the `org.AngelosAura.app.Main` entry point. The proposed layout shows the directories contributors will use when gameplay and content are added.

```text
app/
  src/org/AngelosAura/app/
    Main.java
    runtime/
    game/
      world/           dungeon generation, movement, and shortest paths
      combat/          attacks and enemy prioritization
      inventory/
    presentation/
    content/
    save/
  test/                mirrors the source packages
assets/
  data/
    player.json        starting stats and abilities
    enemies/
    abilities/
    items/
    dungeons/          generation settings and encounter pools
  text/en.json
  art/                 sprite mappings and artwork
  audio/
  maps/                authored maps, if used
```

## 10. Editing Content and Loading Saves

Package the editable `assets/` folder in both the installed app and the release ZIP. Resolve it relative to the installation so launchers work from any working directory. Designers can edit the installed files, restart to try their changes, and bring accepted edits back into the repository's `assets/` folder. Keep field descriptions and examples beside the content.

Use the same validator at startup and in `:app:check`. Catch invalid values, duplicate IDs, unsupported behaviors or effects, and missing references before creating a session. Name the file, entry, and field in each error so the contributor can fix it.

Save player progress, inventory, and the generated dungeon layout separately from authored definitions. Use stable content IDs when a save refers to an enemy or item. If a required ID was removed, report it and preserve the save file. For balance changes, load the current definition and cap saved current HP at the new maximum.

## 11. Performance and Resource Management

Load resources at startup or when entering a map and reuse them during play. Measure gameplay update time and resource use before adding optimizations. Check performance on the lowest-specification supported machine once the hardware target is defined.

## 12. Testing and Observability

Gradle runs JUnit tests from `app/test`. Test dungeon generation, pathfinding, enemy priority, combat, and inventory without opening a window. Use small content fixtures to test validation and save loading.

For the first playable slice, connect one generated room, one enemy, one attack, one sprite, and one name. Verify in the installed build that a designer can edit HP, replace the sprite, change the name, and add a second enemy to the encounter pool. Check that an invalid sprite reference identifies the file and field to fix.

Runtime diagnostics are proposed for frames and updates per second, frame and simulation duration, heap usage, active object counts, and rendering statistics. Keep diagnostics separate from authoritative gameplay logic.

## 13. Alternatives and Deferred Technologies

| Technology or approach | Disposition |
|---|---|
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
6. **Reuse.** Prefer existing libraries when they meet the project's needs.
7. **Incremental complexity.** Introduce abstractions and subsystems when a concrete requirement calls for them.
8. **Measurement-driven optimization.** Base performance changes on observable evidence.
9. **Maintainability.** Favor readable implementations.
10. **Testability.** Keep application logic independently verifiable wherever practical.

## 15. Consequences

One Gradle subproject keeps the build and release process together. The package boundaries let gameplay rules run independently of the desktop backend. Editable content adds a file format and an ID convention that code and content authors share. Validation catches mistakes before they reach a playtest or release.

Fixed-timestep simulation requires timing and input buffering. A dedicated world renderer will need an explicit draw order, and preloading shared assets will add startup work and increase memory use.

## 16. Decision Status

Java is required. Fixed-timestep simulation and a desktop GUI using libGDX, LWJGL3, and OpenGL are accepted. The current baseline uses one Gradle app, a command-line entry point, and JUnit for tests. The package layout and editable content workflow are proposed. The simulation update rate remains open.

The game virtual resolution, tile and sprite dimensions, and camera behavior remain undecided. Hardware compatibility and gameplay performance still need validation.
