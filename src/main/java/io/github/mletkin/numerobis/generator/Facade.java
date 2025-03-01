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
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import io.github.mletkin.numerobis.annotation.GenerateAccessors;
import io.github.mletkin.numerobis.annotation.GenerateBuilder;
import io.github.mletkin.numerobis.plugin.Naming;

/**
 * Configuration for the generation of different builder classes.
 */
public class Facade {

    private boolean productsAreMutable;
    private ListMutatorVariant[] adderVariants;
    private ListMutatorVariant[] mutatorVariants;
    private Naming namingSettings;

    public Facade(boolean productsAreMutable) {
        this(productsAreMutable, Naming.DEFAULT);
    }

    public Facade(boolean productsAreMutable, Naming namingSettings) {
        this.productsAreMutable = productsAreMutable;
        this.namingSettings = namingSettings;
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
     * Generate an embedded builder for a record.
     *
     * @param productUnit
     *                              compilation unit containing the product record
     * @param productTypeName
     *                              name of the product record
     * @return result object with generated source
     */
    public Result forRecordEmbedded(CompilationUnit productUnit, String productTypeName) {
        return Result.builder( //
                new RecordBuilderGenerator(productUnit, productTypeName) //
                        .withNamingSettings(namingSettings) //
                        .withInternalBuilder() //
                        .addFields() //
                        .addMutators() //
                        .addBuildMethod() //
                        .builderUnit() //
        );
    }

    /**
     * Generate a separate builder for a record.
     *
     * @param productUnit
     *                              compilation unit containing the product record
     * @param productTypeName
     *                              name of the product record
     * @param builderUnit
     *                              compilation unit taking the builder
     * @return result object with generated source
     */
    public Result forRecordSeparate(CompilationUnit productUnit, String productTypeName, CompilationUnit builderUnit) {
        return Result.builder( //
                new RecordBuilderGenerator(productUnit, productTypeName) //
                        .withNamingSettings(namingSettings) //
                        .withExternalBuilder(builderUnit) //
                        .addFields() //
                        .addMutators() //
                        .addBuildMethod() //
                        .builderUnit() //
        );
    }

    /**
     * Generate a separate builder using constructor methods.
     *
     * @param productUnit
     *                             compilation unit containing the product class
     * @param productClassName
     *                             name of the product class
     * @param builderUnit
     *                             compilation unit for/with the builder class
     * @return result object with generated source
     */
    public Result withConstructors(CompilationUnit productUnit, String productClassName, CompilationUnit builderUnit) {
        return Result.builder(//
                generatorSeparate(productUnit, productClassName, builderUnit) //
                        .addProductField() //
                        .addConstructors() //
                        .addMutator(mutatorVariants) //
                        .addAdder(adderVariants) //
                        .addBuildMethod() //
                        .builderUnit() //
        );
    }

    /**
     * Generate a separate builder using static factory methods.
     *
     * @param productUnit
     *                             compilation unit containing the product class
     * @param productClassName
     *                             name of the product class
     * @param builderUnit
     *                             compilation unit for/with the builder class
     * @return result object with generated source
     */
    public Result withFactoryMethods(CompilationUnit productUnit, String productClassName,
            CompilationUnit builderUnit) {

        return Result.builder(//
                generatorSeparate(productUnit, productClassName, builderUnit) //
                        .addProductField() //
                        .addFactoryMethods() //
                        .addMutator(mutatorVariants) //
                        .addAdder(adderVariants) //
                        .addBuildMethod() //
                        .builderUnit() //
        );
    }

    private BuilderGenerator generatorSeparate(CompilationUnit productUnit, String productClassName,
            CompilationUnit builderUnit) {
        return new BuilderGenerator(productUnit, productClassName) //
                .mutableByDefault(productsAreMutable) //
                .withNamingSettings(namingSettings) //
                .withExternalBuilder(builderUnit);
    }

    /**
     * Generate an embedded builder using constructor methods.
     *
     * @param productUnit
     *                             compilation unit containing the product class
     * @param productClassName
     *                             name of the product class
     * @return result object with generated source
     */
    public Result withConstructors(CompilationUnit productUnit, String productClassName) {
        return Result.product(//
                generatorEmbedded(productUnit, productClassName) //
                        .addProductField() //
                        .addConstructors() //
                        .addMutator(mutatorVariants) //
                        .addAdder(adderVariants) //
                        .addBuildMethod() //
                        .builderUnit() //
        );
    }

    /**
     * Generate an embedded builder using static factory methods.
     *
     * @param productUnit
     *                             compilation unit containing the product class
     * @param productClassName
     *                             name of the product class
     * @return result object with generated source
     */
    public Result withFactoryMethods(CompilationUnit productUnit, String productClassName) {
        return Result.product(//
                generatorEmbedded(productUnit, productClassName) //
                        .addProductField() //
                        .addFactoryMethods() //
                        .addMutator(mutatorVariants) //
                        .addAdder(adderVariants) //
                        .addBuildMethod() //
                        .builderUnit() //
        );
    }

    private BuilderGenerator generatorEmbedded(CompilationUnit productUnit, String productClassName) {
        return new BuilderGenerator(productUnit, productClassName) //
                .mutableByDefault(productsAreMutable) //
                .withNamingSettings(namingSettings) //
                .withInternalBuilder();
    }

    /**
     * generate accessors for the fields in a class.
     *
     * @param productUnit
     *                        compilation unit with product class
     * @param className
     *                        name of the product class
     * @return compilation unit with the processed product class
     */
    public CompilationUnit withAccessors(CompilationUnit productUnit, String className) {
        return new AccessorGenerator(productUnit, className) //
                .addAccessors() //
                .resultUnit();
    }

    /**
     * Tests whether a class wants a builder.
     *
     * @param sourceUnit
     *                       compilation unit with the potential product class
     * @return {@code true} when a builder class shall be built
     */
    public static boolean isBuilderWanted(CompilationUnit sourceUnit) {
        return sourceUnit.findAll(TypeDeclaration.class).stream() //
                .filter(t -> t instanceof ClassOrInterfaceDeclaration || t instanceof RecordDeclaration) //
                .anyMatch(c -> c.isAnnotationPresent(GenerateBuilder.class));
    }

    /**
     * Test whether a class wants accessors
     *
     * @param sourceUnit
     *                       compilation unit with the potential product class
     * @return {@code true} when accessurs should be generated
     */
    public static boolean areAccessorsWanted(CompilationUnit sourceUnit) {
        return sourceUnit.findAll(ClassOrInterfaceDeclaration.class).stream() //
                .anyMatch(c -> c.isAnnotationPresent(GenerateAccessors.class));
    }

}
