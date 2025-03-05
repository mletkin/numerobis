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

import java.util.Optional;
import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.Type;

import io.github.mletkin.numerobis.annotation.GenerateAdder;
import io.github.mletkin.numerobis.common.PackageVisible;
import io.github.mletkin.numerobis.common.Util;
import io.github.mletkin.numerobis.generator.common.ClassUtil;
import io.github.mletkin.numerobis.generator.common.VariantExtractor;

/**
 * Descriptor for the generation of an adder for a collection field.
 */
@PackageVisible
class AdderMethodDescriptor {

    @PackageVisible
    String fieldName;
    @PackageVisible
    String methodName;
    @PackageVisible
    Type parameterType;
    @PackageVisible
    ListMutatorVariant variant;

    /**
     * Generator for adder method-descriptor-objects.
     * <p>
     * One declaration can contain more than one variable ( e.g. {@code int x,y;})
     */
    @PackageVisible
    static class Generator {
        private static ListMutatorVariant[] DEFAULT = { ListMutatorVariant.ITEM };

        private FieldDeclaration field;
        private ListMutatorVariant[] variants;
        private CompilationUnit cu;
        private String adderPrefix;

        @PackageVisible
        Generator(FieldDeclaration field, ListMutatorVariant[] listMutatorVariant, CompilationUnit cu,
                String adderPrefix) {
            this.field = field;
            this.variants = Util.firstNotEmpty( //
                    new VariantExtractor(GenerateAdder.class).variants(field), //
                    listMutatorVariant) //
                    .orElse(DEFAULT);
            this.cu = cu;
            this.adderPrefix = adderPrefix;
        }

        /**
         * Produces a stream of method descriptors from a field declaration.
         *
         * @return Stream<AdderMethodDescriptor>
         */
        @PackageVisible
        Stream<AdderMethodDescriptor> stream() {
            return field.getVariables().stream() //
                    .filter(vd -> ClassUtil.isCollection(vd, cu)) //
                    .flatMap(this::toVariants);
        }

        private Stream<AdderMethodDescriptor> toVariants(VariableDeclarator vd) {
            return Stream.of(variants) //
                    .filter(v -> v != ListMutatorVariant.NONE) //
                    .map(v -> map(vd, v));
        }

        private AdderMethodDescriptor map(VariableDeclarator vd, ListMutatorVariant variant) {
            AdderMethodDescriptor result = new AdderMethodDescriptor();
            result.methodName = methodName(vd);
            result.fieldName = vd.getNameAsString();
            result.parameterType = vd.getType().asClassOrInterfaceType().getTypeArguments().get().get(0);
            result.variant = variant;
            return result;
        }

        private String methodName(VariableDeclarator vd) {
            return customName().orElseGet(() -> standardAdderName(vd));
        }

        private String standardAdderName(VariableDeclarator vd) {
            return adderPrefix + stripPostfix(Util.firstLetterUppercase(vd.getNameAsString()), "s");
        }

        private Optional<String> customName() {
            return Optional.empty();
        }

        private String stripPostfix(String name, String... postfixes) {
            for (String postfix : postfixes) {
                if (name.endsWith(postfix)) {
                    return name.substring(0, name.length() - postfix.length());
                }
            }
            return name;
        }
    }
}
