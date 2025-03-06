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
import io.github.mletkin.numerobis.plugin.Order;

/**
 * Configuration for the generation of different builder classes.
 */
public class Facade {

    private ListMutatorVariant[] adderVariants = {};
    private ListMutatorVariant[] mutatorVariants = {};
    private boolean productsAreMutable;

    /**
     * Creates a {@code Facade}-Instance and sets the default mutablity.
     *
     * @param productsAreMutable value for the mutability default flag
     */
    public Facade(boolean productsAreMutable) {
        this.productsAreMutable = productsAreMutable;
    }

    /**
     * Sets the variants to generate for list adder methods.
     *
     * @param  adderVariants a list of variants to use
     * @return               the {@code Facade} instance
     */
    public Facade withAdderVariants(ListMutatorVariant[] adderVariants) {
        this.adderVariants = adderVariants;
        return this;
    }

    /**
     * Sets the variants to generate for list mutator methods.
     *
     * @param  mutatorVariants a list of variants to use
     * @return                 the {@code Facade} instance
     */
    public Facade withMutatorVariants(ListMutatorVariant[] mutatorVariants) {
        this.mutatorVariants = mutatorVariants;
        return this;
    }

    /**
     * Creates a generator for an embedded builder for a record.
     *
     * @param  order object descibing the generation process
     * @return       generator
     */
    public Generator forRecordEmbedded(Order order) {
        return () -> new RecordBuilderGenerator(order.productUnit(), order.productType()) //
                .withNamingSettings(order.naming()) //
                .withInternalBuilder() //
                .addFields() //
                .addMutators() //
                .addBuildMethod() //
                .builderUnit();
    }

    /**
     * Creates a generator for a separate builder for a record.
     *
     * @param  order object descibing the generation process
     * @return       generator
     */
    public Generator forRecordSeparate(Order order) {
        return () -> new RecordBuilderGenerator(order.productUnit(), order.productType()) //
                .withNamingSettings(order.naming()) //
                .withExternalBuilder(order.builderUnit()) //
                .addFields() //
                .addMutators() //
                .addBuildMethod() //
                .builderUnit();
    }

    /**
     * Creates a generator for a separate builder using constructor methods.
     *
     * @param  order object descibing the generation process
     * @return       generator
     */
    public Generator separateWithConstructors(Order order) {
        return () -> new BuilderGenerator(order.productUnit(), order.productType()) //
                .mutableByDefault(productsAreMutable) //
                .withNamingSettings(order.naming()) //
                .withExternalBuilder(order.builderUnit()) //
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
     * @param  order object descibing the generation process
     * @return       generator
     */
    public Generator separateWithFactoryMethods(Order order) {
        return () -> new BuilderGenerator(order.productUnit(), order.productType()) //
                .mutableByDefault(productsAreMutable) //
                .withNamingSettings(order.naming()) //
                .withExternalBuilder(order.builderUnit()) //
                .addProductField() //
                .addFactoryMethods() //
                .addMutator(mutatorVariants) //
                .addAdder(adderVariants) //
                .addBuildMethod() //
                .builderUnit();
    }

    /**
     * Creates a generator for an embedded builder using constructor methods.
     *
     * @param  order object descibing the generation process
     * @return       generator
     */
    public Generator embeddedWithConstructors(Order order) {
        return () -> new BuilderGenerator(order.productUnit(), order.productType()) //
                .mutableByDefault(productsAreMutable) //
                .withNamingSettings(order.naming()) //
                .withInternalBuilder() //
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
     * @param  order object descibing the generation process
     * @return       generator
     */
    public Generator embeddedWithFactoryMethods(Order order) {
        return () -> new BuilderGenerator(order.productUnit(), order.productType()) //
                .mutableByDefault(productsAreMutable) //
                .withNamingSettings(order.naming()) //
                .withInternalBuilder() //
                .addProductField() //
                .addFactoryMethods() //
                .addMutator(mutatorVariants) //
                .addAdder(adderVariants) //
                .addBuildMethod() //
                .builderUnit();
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
