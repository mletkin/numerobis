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
package io.github.mletkin.numerobis;

import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import io.github.mletkin.numerobis.generator.ListMutatorVariant;
import io.github.mletkin.numerobis.generator.common.ClassUtil;

/**
 * Helper methods for unit tests.
 */
public final class Fixture {

    private Fixture() {
        // Prevent instantiation
    }

    public static CompilationUnit parse(Path file) {
        try {
            return StaticJavaParser.parse(file);
        } catch (IOException e) {
            fail("IO exception", e);
            return null;
        }
    }

    public static CompilationUnit parse(String product) {
        try {
            return StaticJavaParser.parseResource(product + ".java");
        } catch (IOException e) {
            fail("IO exception", e);
            return null;
        }
    }

    public static CompilationUnit parseString(String clazz) {
        return StaticJavaParser.parse(clazz);
    }

    public static String builder(CompilationUnit unit, String clazz) {
        return asString(extractBuilder(unit, clazz));
    }

    /**
     * Extractes the internal builder class from the product class.
     *
     * @param  cu        compilation unit with the product class
     * @param  className Name of the product class
     * @return           the builder class declaration
     */
    private static ClassOrInterfaceDeclaration extractBuilder(CompilationUnit cu, String className) {
        return ClassUtil.findClass(cu, className) //
                .map(c -> ClassUtil.allMember(c, ClassOrInterfaceDeclaration.class)) //
                .orElseGet(Stream::empty) //
                .findFirst().get();
    }

    public static String product(CompilationUnit unit, String product) {
        return asString(ClassUtil.findClass(unit, product).orElse(null));
    }

    public static ListMutatorVariant[] asArray(ListMutatorVariant variant) {
        if (variant == null) {
            return null;
        }
        ListMutatorVariant[] variants = { variant };
        return variants;
    }

    /**
     * The compilation unt comtent for comparison.
     *
     * @param  cu the compilation unit
     * @return    the content as String
     */
    public static String asString(CompilationUnit cu) {
        return cu.toString().replace("\r", "").replace("\n", "");
    }

    public static String asString(ClassOrInterfaceDeclaration clazz) {
        return clazz.toString().replace("\r", "").replace("\n", "");
    }

}
