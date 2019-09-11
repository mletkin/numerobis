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
 * This clss is used by the mojo as well. This breaks the layering and might be
 * refactored in a future version.
 */
public class Naming {

    /**
     * Default naming settings.
     */
    public static final Naming DEFAULT = new Naming();

    private String factoryMethod = "of";
    private String buildMethod = "build";
    private String mutatorPrefix = "with";
    private String adderPrefix = "add";

    public static class Builder {
        private Naming naming = new Naming();

        private Builder(Naming naming) {
            this.naming = naming;
        }

        public static Builder of() {
            return new Builder(new Naming());
        }

        public static Builder of(Naming naming) {
            return new Builder(naming);
        }

        public Builder withFactoryMethod(String factoryMethod) {
            naming.factoryMethod = factoryMethod;
            return this;
        }

        public Builder withBuildMethod(String buildMethod) {
            naming.buildMethod = buildMethod;
            return this;
        }

        public Builder withMutatorPrefix(String mutatorPrefix) {
            naming.mutatorPrefix = mutatorPrefix;
            return this;
        }

        public Builder withAdderPrefix(String adderPrefix) {
            naming.adderPrefix = adderPrefix;
            return this;
        }

        public Naming build() {
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

    @Override
    public String toString() {
        return "factoryMethod: " + factoryMethod //
                + ", buildMethod: " + buildMethod //
                + ", mutatorPrefix: " + mutatorPrefix //
                + ", adderPrefix: " + adderPrefix;

    }
}
