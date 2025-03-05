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

import java.nio.file.Path;

import com.github.javaparser.ParserConfiguration.LanguageLevel;

import io.github.mletkin.numerobis.annotation.GenerateAdder;
import io.github.mletkin.numerobis.annotation.GenerateListMutator;

/**
 * Parameter container for the {@code Processor} objects.
 */
public final class MojoSettings {

    private Path targetDirectory;
    private BuilderMojo.Creation builderCreation;
    private BuilderMojo.Location builderLocation;
    private boolean productsAreMutable;
    private LanguageLevel javaVersion;
    private GenerateAdder.Variant[] listAdderVariants;
    private GenerateListMutator.Variant[] listMutatorVariants;
    private Naming naming;

    private MojoSettings() {
        // builder instantiation only
    }

    public Path targetDirectory() {
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

    public Naming naming() {
        return naming;
    }

    public LanguageLevel javaVersion() {
        return javaVersion;
    }

    public static class Builder {

        private MojoSettings product;

        public Builder() {
            product = new MojoSettings();
        }

        public Builder withTargetDirectory(String targetDirectory) {
            if (targetDirectory != null && !targetDirectory.isBlank()) {
                product.targetDirectory = Path.of(targetDirectory);
            }
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

        public Builder withJavaVersion(LanguageLevel javaVersion) {
            product.javaVersion = javaVersion;
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

        public Builder withNamingSettings(Naming naming) {
            product.naming = naming;
            return this;
        }

        public MojoSettings build() {
            return product;
        }

    }
}
