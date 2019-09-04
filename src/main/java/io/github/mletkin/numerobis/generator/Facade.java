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

import io.github.mletkin.numerobis.annotation.GenerateAccessors;
import io.github.mletkin.numerobis.annotation.GenerateBuilder;

/**
 * Configuration for the generation of different builder classes.
 */
public class Facade {

    private boolean productsAreMutable;
    private ListMutatorVariant[] adderVariants;
    private ListMutatorVariant[] mutatorVariants;

    public Facade(boolean productsAreMutable) {
        this.productsAreMutable = productsAreMutable;
    }

    public Facade withAdderVariants(ListMutatorVariant[] adderVariants) {
        this.adderVariants = adderVariants;
        return this;
    }

    public Facade withMutatorVariants(ListMutatorVariant[] mutatorVariants) {
        this.mutatorVariants = mutatorVariants;
        return this;
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
    public Result withConstructors(CompilationUnit productUnit, String productClassName, CompilationUnit builderUnit) {

        return Result.builder(//
                new BuilderGenerator(productUnit, productClassName, builderUnit) //
                        .mutableByDefault(productsAreMutable) //
                        .addProductField() //
                        .addConstructors() //
                        .addMutator(mutatorVariants) //
                        .addAdder(adderVariants) //
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
    public Result withFactoryMethods(CompilationUnit productUnit, String productClassName,
            CompilationUnit builderUnit) {

        return Result.builder(//
                new BuilderGenerator(productUnit, productClassName, builderUnit) //
                        .mutableByDefault(productsAreMutable) //
                        .addProductField() //
                        .addFactoryMethods() //
                        .addMutator(mutatorVariants) //
                        .addAdder(adderVariants) //
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
    public Result withConstructors(CompilationUnit productUnit, String productClassName) {
        return Result.product(//
                new BuilderGenerator(productUnit, productClassName) //
                        .mutableByDefault(productsAreMutable) //
                        .addProductField() //
                        .addConstructors() //
                        .addMutator(mutatorVariants) //
                        .addAdder(adderVariants) //
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
    public Result withFactoryMethods(CompilationUnit productUnit, String productClassName) {
        return Result.product(//
                new BuilderGenerator(productUnit, productClassName) //
                        .mutableByDefault(productsAreMutable) //
                        .addProductField() //
                        .addFactoryMethods() //
                        .addMutator(mutatorVariants) //
                        .addAdder(adderVariants) //
                        .addBuildMethod() //
                        .builderUnit() //
        );
    }

    /**
     * generate accessors for the field in a class.
     *
     * @param productUnit
     *            compilation unit with product class
     * @param className
     *            name of the product class
     * @return compilation unit with the processed product class
     */
    public CompilationUnit withAccessors(CompilationUnit productUnit, String className) {
        return new AccessorGenerator(productUnit, className) //
                .addAccessors() //
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
                .anyMatch(c -> c.isAnnotationPresent(GenerateBuilder.class));
    }

    public static boolean areAccessorsWanted(CompilationUnit sourceClass) {
        return sourceClass.findAll(ClassOrInterfaceDeclaration.class).stream()
                .anyMatch(c -> c.isAnnotationPresent(GenerateAccessors.class));
    }

}
