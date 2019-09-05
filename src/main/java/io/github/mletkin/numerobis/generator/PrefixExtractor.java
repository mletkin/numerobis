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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;

/**
 * For extraction of the prefix property from an annotation.
 */
public class PrefixExtractor {

    private static final String PREFIX_FIELD = "prefix";
    private Class<? extends Annotation> annotationClass;

    /**
     * Creates an extractor fot the given annotation class.
     *
     * @param annotationClass
     *            class of the expected annotation
     */
    PrefixExtractor(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * Gets the value of the prefix property from the annotation.
     *
     * @param clazz
     *            class with annotation
     * @return the previx value wrapped in an optional
     */
    Optional<String> prefix(ClassOrInterfaceDeclaration clazz) {
        return clazz.getAnnotationByClass(annotationClass) //
                .flatMap(anno -> findByName(anno, PREFIX_FIELD)) //
                .map(MemberValuePair::getValue) //
                .map(Expression::asStringLiteralExpr) //
                .map(StringLiteralExpr::asString);
    }

    private Optional<MemberValuePair> findByName(AnnotationExpr anno, String parameterName) {
        return anno.findFirst(MemberValuePair.class, mvp -> mvp.getNameAsString().equals(parameterName));
    }

}
