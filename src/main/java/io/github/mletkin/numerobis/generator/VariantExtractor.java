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

import java.lang.annotation.Annotation;
import java.util.Optional;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;

/**
 * For the extraction of variants from a mutator or adder annotation.
 */
class VariantExtractor {

    private static final String LIST_VARIANT_FIELD = "variants";
    private Class<? extends Annotation> annotationClass;

    VariantExtractor(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    ListMutatorVariant[] variants(FieldDeclaration fd) {
        return variantExpressions(fd, LIST_VARIANT_FIELD).stream() //
                .map(Expression::toString) //
                .map(this::extractName) //
                .map(ListMutatorVariant::valueOf) //
                .toArray(ListMutatorVariant[]::new);
    }

    private String extractName(String variant) {
        return variant.substring(variant.lastIndexOf('.') + 1);
    }

    private NodeList<Expression> variantExpressions(FieldDeclaration fd, String parameterName) {
        return fd.getAnnotationByClass(annotationClass) //
                .flatMap(anno -> listVariants(anno, parameterName)) //
                .flatMap(mvp -> mvp.findFirst(ArrayInitializerExpr.class)) //
                .map(ArrayInitializerExpr::getValues) //
                .orElseGet(NodeList::new);
    }

    private Optional<MemberValuePair> listVariants(AnnotationExpr anno, String parameterName) {
        return anno.findFirst(MemberValuePair.class, mvp -> mvp.getNameAsString().equals(parameterName));
    }

}