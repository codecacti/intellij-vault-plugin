import io.gitlab.arturbosch.detekt.Detekt
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.ChangelogSectionUrlBuilder
import org.jetbrains.changelog.date
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.13.3"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "2.0.0"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

val pluginGroup: String = providers.gradleProperty("plugin.group").get()
val projectVersion: String = providers.gradleProperty("version").get()

group = pluginGroup
version = projectVersion

repositories {
    mavenCentral()
}
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.0")
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(providers.gradleProperty("name").get())
    version.set(providers.gradleProperty("intellij.version").get())
    type.set(providers.gradleProperty("intellij.type").get())
    downloadSources.set(providers.gradleProperty("intellij.downloadSources").get().toBoolean())
    updateSinceUntilBuild.set(true)
    plugins.set(providers.gradleProperty("intellij.plugins").get().split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config.setFrom("$projectDir/detekt-config.yml")
    buildUponDefaultConfig = true
}

tasks {
    // Set the compatibility versions to 11
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    withType<Detekt>().configureEach {
        jvmTarget = "1.8"
        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(true)
            // sarif.required.set(true)
            // md.required.set(true)
        }
    }

    // Workaround for KotlinCompilerConfigurableTab memory leak
    // See: https://youtrack.jetbrains.com/issue/KTIJ-699/After-opening-Kotlin-compiler-settings-instance-of-KotlinCompilerConfigurableTab-leaks
    // See: https://youtrack.jetbrains.com/issue/KTIJ-782/java.lang.Throwable-caused-by-buildSearchableOptions-with-LATEST-EAP-SNAPSHOT-for-2020.3-plugin-builds
    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        // See: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml-sincebuild
        sinceBuild.set(providers.gradleProperty("plugin.sinceBuild").get())
        untilBuild.set(providers.gradleProperty("plugin.untilBuild").get())
        pluginId.set(pluginGroup)
        changeNotes.set(
            provider {
                changelog.renderItem(
                    changelog
                        .getUnreleased()
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML
                )
            }
        )
        pluginDescription.set(
            File("./README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"
                containsAll(listOf(start, end)).ifFalse { throw GradleException("Plugin description section not found in README.md:\n$start ... $end") }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run {
                val flavour = CommonMarkFlavourDescriptor()
                val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(this)
                HtmlGenerator(this, parsedTree, flavour).generateHtml()
            }
        )
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // Parse channel from project version number
        // e.g. 1.0.0-alpha.1 => alpha channel
        // See: https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html#specifying-a-release-channel
        val channel: String = projectVersion
            .split('-').getOrElse(1) { "default" }.split('.').first()
        if (listOf("alpha", "beta", "eap").contains(channel)) {
            channels.set(listOf(channel))
        }
    }
}

// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(projectVersion)
    path.set(file("CHANGELOG.md").canonicalPath)
    header.set(provider { "${version.get()} - ${date()}" })
    introduction.set("Fetches credentials for a database from Vault.")
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("Unreleased")
    groups.set(listOf("Added"))
//    lineSeparator.set("\n")
    combinePreReleases.set(true)
    repositoryUrl.set("https://github.com/spencerdcarlson/intellij-vault-plugin")
    sectionUrlBuilder.set(
        ChangelogSectionUrlBuilder { repositoryUrl, currentVersion, previousVersion, isUnreleased ->
            repositoryUrl + when {
                isUnreleased -> when (previousVersion) {
                    null -> "/commits"
                    else -> "/compare/$previousVersion...HEAD"
                }
                previousVersion == null -> "/commits/$currentVersion"
                else -> "/compare/$previousVersion...$currentVersion"
            }
        }
    )
}
