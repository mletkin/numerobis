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

import io.github.mletkin.numerobis.common.Generator;
import io.github.mletkin.numerobis.plugin.Naming;

/**
 * Configuration for the generation of different builder classes.
 */
public class Facade {

    private boolean productsAreMutable;
    private ListMutatorVariant[] adderVariants;
    private ListMutatorVariant[] mutatorVariants;
    private Naming namingSettings;

    /**
     * Creates a {@code Facade}-Instance with default mutablity.
     *
     * @param productsAreMutable value for the mutability default flag
     */
    public Facade(boolean productsAreMutable) {
        this(productsAreMutable, Naming.DEFAULT);
    }

    /**
     * Creates a {@code Facade}-Instance with default mutablity and namings.
     *
     * @param productsAreMutable value for the mutability default flag
     * @param namingSettings     Values for various names
     */
    public Facade(boolean productsAreMutable, Naming namingSettings) {
        this.productsAreMutable = productsAreMutable;
        this.namingSettings = namingSettings;
    }

    /**
     * Sets the variants to generate for list adder methods.
     *
     * @param  adderVariants a list of variants to use
     * @return               the the {@code Facade} instance
     */
    public Facade withAdderVariants(ListMutatorVariant[] adderVariants) {
        this.adderVariants = adderVariants;
        return this;
    }

    /**
     * Sets the variants to generate for list mutator methods.
     *
     * @param  mutatorVariants a list of variants to use
     * @return                 the the {@code Facade} instance
     */
    public Facade withMutatorVariants(ListMutatorVariant[] mutatorVariants) {
        this.mutatorVariants = mutatorVariants;
        return this;
    }

    /**
     * Creates a generator for an embedded builder for a record.
     *
     * @param  productUnit     compilation unit containing the product record
     * @param  productTypeName name of the product record
     * @return                 generator
     */
    public Generator forRecordEmbedded(CompilationUnit productUnit, String productTypeName) {
        return () -> new RecordBuilderGenerator(productUnit, productTypeName) //
                .withNamingSettings(namingSettings) //
                .withInternalBuilder() //
                .addFields() //
                .addMutators() //
                .addBuildMethod() //
                .builderUnit();
    }

    /**
     * Creates a generator for a separate builder for a record.
     *
     * @param  productUnit     compilation unit containing the product record
     * @param  productTypeName name of the product record
     * @param  builderUnit     compilation unit taking the builder
     * @return                 generator
     */
    public Generator forRecordSeparate(CompilationUnit productUnit, String productTypeName,
            CompilationUnit builderUnit) {
        return () -> new RecordBuilderGenerator(productUnit, productTypeName) //
                .withNamingSettings(namingSettings) //
                .withExternalBuilder(builderUnit) //
                .addFields() //
                .addMutators() //
                .addBuildMethod() //
                .builderUnit();
    }

    /**
     * Creates a generator for a separate builder using constructor methods.
     *
     * @param  productUnit      compilation unit containing the product class
     * @param  productClassName name of the product class
     * @param  builderUnit      compilation unit for/with the builder class
     * @return                  generator
     */
    public Generator withConstructors(CompilationUnit productUnit, String productClassName,
            CompilationUnit builderUnit) {
        return () -> generatorSeparate(productUnit, productClassName, builderUnit) //
                .addProductField() //
                .addConstructors() //
                .addMutator(mutatorVariants) //
                .addAdder(adderVariants) //
                .addBuildMethod() //
                .builderUnit();
    }

    /**
     * Creates a generator for a separate builder using static factory methods.
     *
     * @param  productUnit      compilation unit containing the product class
     * @param  productClassName name of the product class
     * @param  builderUnit      compilation unit for/with the builder class
     * @return                  generator
     */
    public Generator withFactoryMethods(CompilationUnit productUnit, String productClassName,
            CompilationUnit builderUnit) {

        return () -> generatorSeparate(productUnit, productClassName, builderUnit) //
                .addProductField() //
                .addFactoryMethods() //
                .addMutator(mutatorVariants) //
                .addAdder(adderVariants) //
                .addBuildMethod() //
                .builderUnit();
    }

    private BuilderGenerator generatorSeparate(CompilationUnit productUnit, String productClassName,
            CompilationUnit builderUnit) {
        return new BuilderGenerator(productUnit, productClassName) //
                .mutableByDefault(productsAreMutable) //
                .withNamingSettings(namingSettings) //
                .withExternalBuilder(builderUnit);
    }

    /**
     * Creates a generator for an embedded builder using constructor methods.
     *
     * @param  productUnit      compilation unit containing the product class
     * @param  productClassName name of the product class
     * @return                  generator
     */
    public Generator withConstructors(CompilationUnit productUnit, String productClassName) {
        return () -> generatorEmbedded(productUnit, productClassName) //
                .addProductField() //
                .addConstructors() //
                .addMutator(mutatorVariants) //
                .addAdder(adderVariants) //
                .addBuildMethod() //
                .builderUnit();
    }

    /**
     * Creates a generator for an embedded builder using static factory methods.
     *
     * @param  productUnit      compilation unit containing the product class
     * @param  productClassName name of the product class
     * @return                  generator
     */
    public Generator withFactoryMethods(CompilationUnit productUnit, String productClassName) {
        return () -> generatorEmbedded(productUnit, productClassName) //
                .addProductField() //
                .addFactoryMethods() //
                .addMutator(mutatorVariants) //
                .addAdder(adderVariants) //
                .addBuildMethod() //
                .builderUnit();
    }

    private BuilderGenerator generatorEmbedded(CompilationUnit productUnit, String productClassName) {
        return new BuilderGenerator(productUnit, productClassName) //
                .mutableByDefault(productsAreMutable) //
                .withNamingSettings(namingSettings) //
                .withInternalBuilder();
    }

    /**
     * Generates accessors for the fields in a class.
     *
     * @param  productUnit compilation unit with product class
     * @param  className   name of the product class
     * @return             compilation unit with the processed product class
     */
    public CompilationUnit withAccessors(CompilationUnit productUnit, String className) {
        return new AccessorGenerator(productUnit, className) //
                .addAccessors() //
                .resultUnit();
    }

}
