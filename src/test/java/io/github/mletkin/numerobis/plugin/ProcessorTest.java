/**
 * (c) 2025 by Ullrich Rieger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.mletkin.numerobis.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.mletkin.numerobis.plugin.BuilderMojo.Creation;
import io.github.mletkin.numerobis.plugin.BuilderMojo.Location;

class ProcessorTest {

    @Nested
    class BuilderPath {

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
}
