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

import java.io.IOException;
import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import io.github.mletkin.numerobis.generator.ClassUtil;

/**
 * Utilities for unit tests.
 */
public class Util {

    /**
     * Extractes the internal builder class from the product class.
     *
     * @param cu
     *            compilation unit with the product class
     * @param className
     *            Name of the product class
     * @return the builder class declaration
     */
    static ClassOrInterfaceDeclaration extractBuilder(CompilationUnit cu, String className) {
        return ClassUtil.findClass(cu, className) //
                .map(c -> ClassUtil.allMember(c, ClassOrInterfaceDeclaration.class)) //
                .orElseGet(Stream::empty) //
                .findFirst().get();
    }

    /**
     * The compilation unt comtent for comparison.
     *
     * @param cu
     *            the compilation unit
     * @return the content as String
     */
    static String asString(CompilationUnit cu) {
        return cu.toString().replace("\r\n", "");
    }

    static String asString(ClassOrInterfaceDeclaration clazz) {
        return clazz.toString().replace("\r\n", "");
    }

    interface Thrower<T> {
        T call() throws IOException;
    }

    static <T> T uncheckExceptions(Thrower<T> fkt) {
        try {
            return fkt.call();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
