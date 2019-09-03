package io.github.mletkin.numerobis.plugin;

import io.github.mletkin.numerobis.annotation.GenerateAdder.Variant;

public class Order {

    String targetDirectory;
    BuilderMojo.Creation builderCreation;
    BuilderMojo.Location builderLocation;
    boolean productsAreMutable;
    Variant[] listAdderVariants;
    Variant[] listMutatorVariants;

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

    public Variant[] listAdderVariants() {
        return listAdderVariants;
    }

    public Variant[] listMutatorVariants() {
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

        public Builder withListAdderVariants(Variant[] listAdderVariants) {
            product.listAdderVariants = listAdderVariants;
            return this;
        }

        public Builder withListMutatorVariants(Variant[] listMutatorVariants) {
            product.listMutatorVariants = listMutatorVariants;
            return this;
        }

        public Order build() {
            return product;
        }
    }
}
