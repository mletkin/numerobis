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
package io.github.mletkin.numerobis.plugin;

import io.github.mletkin.numerobis.annotation.GenerateAdder;
import io.github.mletkin.numerobis.annotation.GenerateListMutator;

/**
 * Parameter container for the {@code Porcessor} objects.
 */
public class Order {

    String targetDirectory;
    BuilderMojo.Creation builderCreation;
    BuilderMojo.Location builderLocation;
    boolean productsAreMutable;
    GenerateAdder.Variant[] listAdderVariants;
    GenerateListMutator.Variant[] listMutatorVariants;

    private Order() {
        // builder instantiation only
    }

    public String targetDirectory() {
        return targetDirectory;
    }

    public BuilderMojo.Creation builderCreation() {
        return builderCreation;
    }

    public BuilderMojo.Location builderLocation() {
        return builderLocation;
    }

    public boolean productsAreMutable() {
        return productsAreMutable;
    }

    public GenerateAdder.Variant[] listAdderVariants() {
        return listAdderVariants;
    }

    public GenerateListMutator.Variant[] listMutatorVariants() {
        return listMutatorVariants;
    }

    public static class Builder {

        private Order product;

        public Builder() {
            product = new Order();
        }

        public Builder withTargetDirectory(String targetDirectory) {
            product.targetDirectory = targetDirectory;
            return this;
        }

        public Builder withBuilderCreation(BuilderMojo.Creation builderCreation) {
            product.builderCreation = builderCreation;
            return this;
        }

        public Builder withBuilderLocation(BuilderMojo.Location builderLocation) {
            product.builderLocation = builderLocation;
            return this;
        }

        public Builder withProductsAreMutable(boolean productsAreMutable) {
            product.productsAreMutable = productsAreMutable;
            return this;
        }

        public Builder withListAdderVariants(GenerateAdder.Variant[] listAdderVariants) {
            product.listAdderVariants = listAdderVariants;
            return this;
        }

        public Builder withListMutatorVariants(GenerateListMutator.Variant[] listMutatorVariants) {
            product.listMutatorVariants = listMutatorVariants;
            return this;
        }

        public Order build() {
            return product;
        }
    }
}
