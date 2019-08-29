/**
 * (c) 2019 by Ullrich Rieger
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

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.javaparser.StaticJavaParser;

import io.github.mletkin.numerobis.Util;

class BuilderGeneratorTest {

    @ParameterizedTest
    @MethodSource()
    void mutableByDefault(String classname, boolean isMutable) {
        Assertions.assertThat(generator(classname).mutableByDefault(true).isProductMutable()).isEqualTo(isMutable);
    }

    static Stream<Arguments> mutableByDefault() {
        return Stream.of(//
                Arguments.of("Empty", true), //
                Arguments.of("Mutable", true), //
                Arguments.of("Immutable", false) //
        );
    }

    @ParameterizedTest
    @MethodSource()
    void immutableByDefault(String classname, boolean isMutable) {
        Assertions.assertThat(generator(classname).mutableByDefault(false).isProductMutable()).isEqualTo(isMutable);
    }

    static Stream<Arguments> immutableByDefault() {
        return Stream.of(//
                Arguments.of("Empty", false), //
                Arguments.of("Mutable", true), //
                Arguments.of("Immutable", false) //
        );
    }

    BuilderGenerator generator(String className) {
        return Util.uncheckExceptions(
                () -> new BuilderGenerator(StaticJavaParser.parseResource(className + ".java"), className));
    }
}
