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
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.Type;

import io.github.mletkin.numerobis.annotation.GenerateAdder.Variant;
import io.github.mletkin.numerobis.annotation.GenerateMutator;
import io.github.mletkin.numerobis.common.Util;

/**
 * Descriptor for the generation of a mutator for a field.
 */
class MutatorMethodDescriptor {

    String methodName;
    String parameterName;
    Type parameterType; // actually the field type
    Variant variant;

    /**
     * Generator for mutator descriptor objects.
     * <p>
     * One declaration can contain more than one variable ( e.g. {@code int x,y;})
     */
    static class Generator {
        private static Variant[] DEFAULT = { Variant.OBJECT };

        private FieldDeclaration field;
        private Variant[] variants;
        private CompilationUnit cu;

        Generator(FieldDeclaration field, Variant[] variants, CompilationUnit cu) {
            this.field = field;
            this.variants = Util.firstNotEmpty( //
                    new VariantExtractor(GenerateMutator.class).variants(field), //
                    variants) //
                    .orElse(DEFAULT);
            this.cu = cu;
        }

        /**
         * Produces a stream of method descriptors from a field declaration.
         *
         * @return Stream<MutatorMethodDescriptor>
         */
        Stream<MutatorMethodDescriptor> stream() {
            return field.getVariables().stream() //
                    .flatMap(this::toVariants);
        }

        private Stream<MutatorMethodDescriptor> toVariants(VariableDeclarator vd) {
            if (ClassUtil.implementsCollection(vd, cu)) {
                return Stream.of(variants) //
                        .filter(v -> v != Variant.NONE) //
                        .map(v -> map(vd, v));
            } else {
                return Stream.of(map(vd, Variant.OBJECT));
            }
        }

        private MutatorMethodDescriptor map(VariableDeclarator vd, Variant variant) {
            MutatorMethodDescriptor result = new MutatorMethodDescriptor();
            result.methodName = methodName(vd);
            result.parameterName = vd.getNameAsString();
            result.parameterType = vd.getType();
            result.variant = variant;
            return result;
        }

        private String methodName(VariableDeclarator vd) {
            return customName().orElseGet(() -> standardMutatorName(vd));
        }

        private String standardMutatorName(VariableDeclarator vd) {
            return BuilderGenerator.MUTATOR_PREFIX + Util.firstLetterUppercase(vd.getNameAsString());
        }

        private Optional<String> customName() {
            return field.getAnnotationByClass(GenerateMutator.class) //
                    .flatMap(AnnotationExpr::toNormalAnnotationExpr) //
                    .map(a -> value(a, "name"));
        }

        private String value(NormalAnnotationExpr anno, String name) {
            return anno.getPairs().stream().filter(p -> p.getNameAsString().equals(name)).findFirst()
                    .map(MemberValuePair::getValue) //
                    .map(Expression::asStringLiteralExpr) //
                    .map(StringLiteralExpr::getValue) //
                    .orElse(null);
        }
    }
}
