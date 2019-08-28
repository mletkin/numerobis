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

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.Type;

import io.github.mletkin.numerobis.annotation.WithMethod;

/**
 * Descriptor for the generation of a mutator for a field.
 */
class MutatorMethodDescriptor {

    String methodName;
    String parameterName;
    Type parameterType;

    /**
     * Generator for withMethod-Descriptor-Objects.
     * <p>
     * One declaration can contain more than one variable ( e.g. {@code int x,y;})
     */
    static class Generator {

        FieldDeclaration field;

        Generator(FieldDeclaration field) {
            this.field = field;
        }

        /**
         * Produces a stream of method Descriptors from a field declaration.
         *
         * @return Stream<MutatorMethodDescriptor>
         */
        Stream<MutatorMethodDescriptor> stream() {
            return field.getVariables().stream() //
                    .map(this::map);
        }

        private MutatorMethodDescriptor map(VariableDeclarator vd) {
            MutatorMethodDescriptor result = new MutatorMethodDescriptor();
            result.methodName = methodName(vd);
            result.parameterName = vd.getNameAsString();
            result.parameterType = vd.getType();
            return result;
        }

        private String methodName(VariableDeclarator vd) {
            return customName().orElseGet(() -> makeWithName(vd));
        }

        private String makeWithName(VariableDeclarator vd) {
            String name = vd.getNameAsString();
            return BuilderGenerator.MUTATOR_PREFIX + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

        private Optional<String> customName() {
            return field.getAnnotationByClass(WithMethod.class) //
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
