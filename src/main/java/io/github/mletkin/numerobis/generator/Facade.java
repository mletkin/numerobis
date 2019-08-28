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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import io.github.mletkin.numerobis.annotation.AccessMethods;
import io.github.mletkin.numerobis.annotation.WithBuilder;

/**
 * Configuration for the generation of different builder classes.
 */
public final class Facade {

    private Facade() {
        // prevent instantiation
    }

    public static class Result {
        public CompilationUnit productUnit;
        public CompilationUnit builderUnit;

        static Result builder(CompilationUnit unit) {
            Result result = new Result();
            result.builderUnit = unit;
            return result;
        }

        static Result product(CompilationUnit unit) {
            Result result = new Result();
            result.productUnit = unit;
            return result;
        }
    }

    /**
     * Generate a builder for a product class using constructor methods.
     *
     * @param productUnit
     *            compilation unit containing the product class
     * @param productClassName
     *            name of the product class
     * @param builderUnit
     *            compilation unit for/with the builder class
     * @return result object with generated source
     */
    public static Result withConstructors(CompilationUnit productUnit, String productClassName,
            CompilationUnit builderUnit) {

        return Result.builder(//
                new BuilderGenerator(productUnit, productClassName, builderUnit) //
                        .addProductField() //
                        .addConstructors() //
                        .addMutator() //
                        .addAdderMethods() //
                        .addBuildMethod() //
                        .builderUnit() //
        );
    }

    /**
     * Generate a builder for a product class using static factory methods.
     *
     * @param productUnit
     *            compilation unit containing the product class
     * @param productClassName
     *            name of the product class
     * @param builderUnit
     *            compilation unit for/with the builder class
     * @return result object with generated source
     */
    public static Result withFactoryMethods(CompilationUnit productUnit, String productClassName,
            CompilationUnit builderUnit) {

        return Result.builder(//
                new BuilderGenerator(productUnit, productClassName, builderUnit) //
                        .addProductField() //
                        .addFactoryMethods() //
                        .addMutator() //
                        .addAdderMethods() //
                        .addBuildMethod() //
                        .builderUnit() //
        );
    }

    /**
     * Generate a builder for a product class using constructor methods.
     *
     * @param productUnit
     *            compilation unit containing the product class
     * @param productClassName
     *            name of the product class
     * @return result object with generated source
     */
    public static Result withConstructors(CompilationUnit productUnit, String productClassName) {
        return Result.product(//
                new BuilderGenerator(productUnit, productClassName) //
                        .addProductField() //
                        .addConstructors() //
                        .addMutator() //
                        .addAdderMethods() //
                        .addBuildMethod() //
                        .builderUnit() //
        );
    }

    /**
     * Generate an internal builder for a product class using static factory
     * methods.
     *
     * @param productUnit
     *            compilation unit containing the product class
     * @param productClassName
     *            name of the product class
     * @return result object with generated source
     */
    public static Result withFactoryMethods(CompilationUnit productUnit, String productClassName) {
        return Result.product(//
                new BuilderGenerator(productUnit, productClassName) //
                        .addProductField() //
                        .addFactoryMethods() //
                        .addMutator() //
                        .addAdderMethods() //
                        .addBuildMethod() //
                        .builderUnit() //
        );
    }

    public static CompilationUnit withAccessMethods(CompilationUnit productUnit, String className) {
        return new AccessorGenerator(productUnit, className) //
                .addAccessMethods() //
                .resultUnit();
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
                .anyMatch(c -> c.isAnnotationPresent(WithBuilder.class));
    }

    public static boolean areAccessorsWanted(CompilationUnit sourceClass) {
        return sourceClass.findAll(ClassOrInterfaceDeclaration.class).stream()
                .anyMatch(c -> c.isAnnotationPresent(AccessMethods.class));
    }

}
