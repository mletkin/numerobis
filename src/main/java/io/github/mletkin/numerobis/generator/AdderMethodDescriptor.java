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
import io.github.mletkin.numerobis.annotation.GenerateAdder.Variant;
import io.github.mletkin.numerobis.common.Util;

/**
 * Descriptor for the generation of an adder for a collection field.
 */
class AdderMethodDescriptor {

    String fieldName;
    String methodName;
    Type parameterType;
    Variant variant;

    /**
     * Generator for withMethod-Descriptor-Objects.
     * <p>
     * One declaration can contain more than one variable ( e.g. {@code int x,y;})
     */
    static class Generator {
        private static Variant[] DEFAULT = { Variant.ITEM };

        private FieldDeclaration field;
        private Variant[] variants;
        private CompilationUnit cu;

        Generator(FieldDeclaration field, Variant[] variants, CompilationUnit cu) {
            this.field = field;
            this.variants = Util.firstNotEmpty( //
                    new VariantExtractor(GenerateAdder.class).variants(field), //
                    variants) //
                    .orElse(DEFAULT);
            this.cu = cu;
        }

        /**
         * Produces a stream of method descriptors from a field declaration.
         *
         * @return Stream<AdderMethodDescriptor>
         */
        Stream<AdderMethodDescriptor> stream() {
            return field.getVariables().stream() //
                    .filter(vd -> ClassUtil.implementsCollection(vd, cu)) //
                    .flatMap(this::toVariants);
        }

        private Stream<AdderMethodDescriptor> toVariants(VariableDeclarator vd) {
            return Stream.of(variants) //
                    .filter(v -> v != Variant.NONE) //
                    .map(v -> map(vd, v));
        }

        private AdderMethodDescriptor map(VariableDeclarator vd, Variant variant) {
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
            return BuilderGenerator.ADDER_PREFIX + //
                    stripPostfix(Util.firstLetterUppercase(vd.getNameAsString()), "en", "e", "s");
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
