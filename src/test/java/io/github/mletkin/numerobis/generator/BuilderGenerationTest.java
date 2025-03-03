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
package io.github.mletkin.numerobis.generator;

import static io.github.mletkin.numerobis.Fixture.parse;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

/**
 * Tests generation of builders.
 */
class BuilderGenerationTest {

    private static final Path RSCE = Path.of("src/test/resources");
    private static final ListMutatorVariant[] NONE = {};

    {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(LanguageLevel.JAVA_17);
    }

    @Nested
    class Class {

        @Test
        void generatesEmbeddedBuilder() {
            var cu = parse("TestClass");
            var builder = new BuilderGenerator(cu, "TestClass") //
                    .withInternalBuilder() //
                    .addProductField() //
                    .addConstructors() //
                    .addMutator(NONE) //
                    .addBuildMethod() //
                    .builderUnit();

            assertThat(RSCE.resolve("internal").resolve("TestClass")).hasContent(builder.toString());
        }

        @Test
        void existingEmbeddedBuilderIsNotChanged() {
            var cu = parse(RSCE.resolve("internal").resolve("TestClass"));
            var builder = new BuilderGenerator(cu, "TestClass") //
                    .withInternalBuilder() //
                    .addProductField() //
                    .addConstructors() //
                    .addMutator(NONE) //
                    .addBuildMethod() //
                    .builderUnit();

            assertThat(RSCE.resolve("internal").resolve("TestClass")).hasContent(builder.toString());
        }

        @Test
        void generatesSeparatedBuilder() {
            var cu = parse("TestClass");
            var builder = new BuilderGenerator(cu, "TestClass") //
                    .withExternalBuilder(new CompilationUnit()) //
                    .addProductField() //
                    .addConstructors() //
                    .addMutator(NONE) //
                    .addBuildMethod() //
                    .builderUnit();

            assertThat(RSCE.resolve("external").resolve("TestClass")).hasContent(builder.toString());
        }

        @Test
        void existingSeparatedBuilderIsNotChanged() {
            var cu = parse("TestClass");
            var builder = new BuilderGenerator(cu, "TestClass") //
                    .withExternalBuilder(new CompilationUnit()) //
                    .addProductField() //
                    .addConstructors() //
                    .addMutator(NONE) //
                    .addBuildMethod() //
                    .builderUnit();

            assertThat(RSCE.resolve("external").resolve("TestClass")).hasContent(builder.toString());
        }
    }

    @Nested
    class Record {

        @Test
        void generatesEmbeddedBuilder() {
            var cu = parse("TestRecord");
            var builder = new RecordBuilderGenerator(cu, "TestRecord") //
                    .withInternalBuilder() //
                    .addFields() //
                    .addMutators() //
                    .addBuildMethod() //
                    .builderUnit();

            assertThat(RSCE.resolve("internal").resolve("TestRecord")).hasContent(builder.toString());
        }

        @Test
        void existingEmbeddedBuilderIsNotChanged() {
            var cu = parse(RSCE.resolve("internal").resolve("TestRecord"));
            var builder = new RecordBuilderGenerator(cu, "TestRecord") //
                    .withInternalBuilder() //
                    .addFields() //
                    .addMutators() //
                    .addBuildMethod() //
                    .builderUnit();

            assertThat(RSCE.resolve("internal").resolve("TestRecord")).hasContent(builder.toString());
        }

        @Test
        void generatesSeparatedBuilder() {
            var bcu = parse(RSCE.resolve("external").resolve("TestRecord"));
            var cu = parse(RSCE.resolve("TestRecord.java"));
            var builder = new RecordBuilderGenerator(cu, "TestRecord") //
                    .withExternalBuilder(bcu) //
                    .addFields() //
                    .addMutators() //
                    .addBuildMethod() //
                    .builderUnit();

            assertThat(RSCE.resolve("external").resolve("TestRecord")).hasContent(builder.toString());
        }

        @Test
        void existingSeparatedBuilderIsNotChanged() {
            var bcu = parse(RSCE.resolve("external").resolve("TestRecord"));
            var cu = parse(RSCE.resolve("TestRecord.java"));
            var builder = new RecordBuilderGenerator(cu, "TestRecord") //
                    .withExternalBuilder(bcu) //
                    .addFields() //
                    .addMutators() //
                    .addBuildMethod() //
                    .builderUnit();

            assertThat(RSCE.resolve("external").resolve("TestRecord")).hasContent(builder.toString());
        }

    }

}
