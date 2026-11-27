import org.gradle.jvm.application.tasks.CreateStartScripts

val gdxVersion = "1.14.2"
val lwjglVersion = "3.4.3"

repositories {
    mavenCentral()
}

plugins {
    application
    id("com.diffplug.spotless") version "8.12.0"
}

version = "0.1.0"

tasks.register("printVersion") {
    doLast { println(project.version) }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    runtimeOnly("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.1")

    constraints {
        implementation("org.lwjgl:lwjgl:$lwjglVersion")
        implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
        implementation("org.lwjgl:lwjgl-jemalloc:$lwjglVersion")
        implementation("org.lwjgl:lwjgl-openal:$lwjglVersion")
        implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
        implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")
    }
}

tasks.test {
    useJUnitPlatform()
    systemProperty("dnd.assets", rootProject.layout.projectDirectory.dir("assets").asFile.absolutePath)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

spotless {
    java {
        googleJavaFormat().aosp()
    }
}

application {
    // Define the main class for the application.
    mainClass = "org.AngelosAura.app.Main"

    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
        "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED",
    )
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
    }
    test {
        java.setSrcDirs(listOf("test"))
    }
}

val gameAssets = rootProject.layout.projectDirectory.dir("assets")

distributions {
    main {
        contents {
            from(gameAssets) {
                into("assets")
                exclude("**/*.tiled-session")
            }
        }
    }
}

tasks.named<Zip>("distZip") {
    archiveVersion = ""
}

tasks.named<JavaExec>("run") {
    systemProperty("dnd.assets", gameAssets.asFile.absolutePath)
    if (providers.systemProperty("os.name").get().startsWith("Mac")) {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        val script = unixScript.readText()
        val launch = "exec \"\$JAVACMD\" \"\$@\""
        check(launch in script.lineSequence()) { "Gradle's Unix launcher command changed" }
        // The distribution can be built on Linux and launched on macOS.
        val macJvmArg = "if \"\$darwin\"; then\n    set -- -XstartOnFirstThread \"\$@\"\nfi"
        unixScript.writeText(script.replace(launch, "$macJvmArg\n\n$launch"))
    }
}
