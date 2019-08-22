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

import static io.github.mletkin.numerobis.common.Util.ifNotThrow;
import static io.github.mletkin.numerobis.generator.ProductUtil.containsClass;
import static io.github.mletkin.numerobis.generator.ProductUtil.hasUsableConstructor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import io.github.mletkin.numerobis.annotation.WithBuilder;

/**
 * Configuration for the generation of different builder classes.
 */
public final class Facade {

    private Facade() {
        // prevent instantiation
    }

    /**
     * Generate a builder for a product class using constructor methods.
     *
     * @param product
     *            compilation unit containing the product class
     * @param producClassName
     *            name of the product class
     * @param builder
     *            compilation unit for the builder class
     * @return compilation unit containing the builder classs
     */
    public static CompilationUnit withConstructors(CompilationUnit product, String producClassName, CompilationUnit builder) {
        ifNotThrow(containsClass(product, producClassName), GeneratorException::productClassNotFound);
        ifNotThrow(hasUsableConstructor(product), GeneratorException::noConstructorFound);

        return new BuilderGenerator(product, producClassName, builder) //
                .addProductField() //
                .addConstructors() //
                .addWithMethods() //
                .addAddMethods() //
                .addBuildMethod() //
                .builderUnit();
    }

    /**
     * Generate a builder for a product class using static factory methods.
     *
     * @param product
     *            compilation unit containing the product class
     * @param producClassName
     *            name of the product class
     * @param builder
     *            compilation unit for the builder class
     * @return compilation unit containing the builder classs
     */
    public static CompilationUnit withFactoryMethods(CompilationUnit product, String producClassName,
            CompilationUnit builder) {
        ifNotThrow(containsClass(product, producClassName), GeneratorException::productClassNotFound);
        ifNotThrow(hasUsableConstructor(product), GeneratorException::noConstructorFound);

        return new BuilderGenerator(product, producClassName, builder) //
                .addProductField() //
                .addFactoryMethods() //
                .addWithMethods() //
                .addAddMethods() //
                .addBuildMethod() //
                .builderUnit();
    }

    /**
     * Tests whether a class needs a builder.
     *
     * @param sourceClass
     *            compilation unit with the potential product class
     * @return {@code true} when a builder class shall be built
     */
    public static boolean isBuilderWanted(CompilationUnit sourceClass) {
        return sourceClass.findAll(ClassOrInterfaceDeclaration.class).stream()
                .anyMatch(c -> c.isAnnotationPresent(WithBuilder.class.getSimpleName()));
    }

}
