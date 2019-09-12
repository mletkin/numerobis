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

/**
 * Naming settings for the generator.
 * <p>
 * This class is used by the mojo as well.<br>
 * This breaks the layering and might be refactored in a future version.<br>
 * Default settings are defined by the values that are assigned to the fields in
 * intantiation.
 */
public class Naming {

    /**
     * An instance with all naming sttings set to the default value.
     */
    public static final Naming DEFAULT = new Naming();

    private String factoryMethod = "of";
    private String buildMethod = "build";
    private String mutatorPrefix = "with";
    private String adderPrefix = "add";
    private String builderClassPostfix = "Builder";
    private String productField = "product";

    /**
     * Object instances are usually generated through maven calling the mojo.
     * <p>
     * The builder is for unit test use only and hence package visible.
     */
    static class Builder {
        private Naming naming = new Naming();

        private Builder() {
            // instantiation through factory method only
        }

        static Builder of() {
            return new Builder();
        }

        Builder withFactoryMethod(String factoryMethod) {
            naming.factoryMethod = factoryMethod;
            return this;
        }

        Builder withBuildMethod(String buildMethod) {
            naming.buildMethod = buildMethod;
            return this;
        }

        Builder withMutatorPrefix(String mutatorPrefix) {
            naming.mutatorPrefix = mutatorPrefix;
            return this;
        }

        Builder withAdderPrefix(String adderPrefix) {
            naming.adderPrefix = adderPrefix;
            return this;
        }

        Builder withBuilderClassPostfix(String builderClassPostfix) {
            naming.builderClassPostfix = builderClassPostfix;
            return this;
        }

        Builder withProductField(String productField) {
            naming.productField = productField;
            return this;
        }

        Naming build() {
            return naming;
        }

    }

    public String factoryMethod() {
        return factoryMethod;
    }

    public String buildMethod() {
        return buildMethod;
    }

    public String mutatorPrefix() {
        return mutatorPrefix;
    }

    public String adderPrefix() {
        return adderPrefix;
    }

    public String builderClassPostfix() {
        return builderClassPostfix;
    }

    public String productField() {
        return productField;
    }

    @Override
    public String toString() {
        return "factoryMethod: " + factoryMethod //
                + ", buildMethod: " + buildMethod //
                + ", mutatorPrefix: " + mutatorPrefix //
                + ", adderPrefix: " + adderPrefix //
                + ", builderClassPostfix: " + builderClassPostfix //
                + ", productField: " + productField;
    }
}
