package io.github.mletkin.numerobis.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.mletkin.numerobis.plugin.BuilderMojo.Creation;
import io.github.mletkin.numerobis.plugin.BuilderMojo.Location;

class ProcessorTest {

    @ParameterizedTest
    @ValueSource(strings = { "", " " })
    @NullSource
    void noDestinationPath(String targetDir) {
        var p = processor(targetDir);

        assertThat(p.builderPath(Path.of("foo/bar/baz.java"), "org.mletkin.test")) //
                .isEqualTo(Path.of("foo/bar/bazBuilder.java"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " " })
    @NullSource
    void noDestinationPathAbsoluteProductPath(String targetDir) {
        var p = processor(targetDir);

        assertThat(p.builderPath(Path.of("c:/foo/bar/baz.java"), "org.mletkin.test")) //
                .isEqualTo(Path.of("c:/foo/bar/bazBuilder.java"));
    }

    @Test
    void withDestinationPath() {
        var p = processor("target/generated");

        assertThat(p.builderPath(Path.of("foo/bar/baz.java"), "org.mletkin.test")) //
                .isEqualTo(Path.of("target/generated/org/mletkin/test/bazBuilder.java"));
    }

    @Test
    void withAbsoluteDestinationPath() {
        var p = processor("c:/target/generated");

        assertThat(p.builderPath(Path.of("d:/foo/bar/baz.java"), "org.mletkin.test")) //
                .isEqualTo(Path.of("c:/target/generated/org/mletkin/test/bazBuilder.java"));
    }

    @Test
    void nonJavaFile() {
        var p = processor(null);

        assertThat(p.builderPath(Path.of("foo/bar/baz.txt"), "org.mletkin.test")) //
                .isEqualTo(Path.of("foo/bar/baz.txt"));

    }

    private Processor processor(String targetDirectory) {
        return new Processor(new MojoSettings.Builder() //
                .withBuilderCreation(Creation.CONSTRUCTOR) //
                .withBuilderLocation(Location.EMBEDDED) //
                .withNamingSettings(Naming.DEFAULT) //
                .withTargetDirectory(targetDirectory) //
                .build());
    }
}
