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
package io.github.mletkin.numerobis.generator.common;

import java.lang.annotation.Annotation;
import java.util.Optional;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

/**
 * For extraction of a string property from an annotation.
 */
public class StringExtractor {

    private Class<? extends Annotation> annotationClass;
    private String name;

    /**
     * Creates an extractor for the given annotation class.
     *
     * @param annotationClass
     *            class of the expected annotation
     * @param name
     *            name of the property to extract
     */
    public StringExtractor(Class<? extends Annotation> annotationClass, String name) {
        this.annotationClass = annotationClass;
        this.name = name;
    }

    /**
     * Gets the value of the property from the annotation.
     *
     * @param node
     *            node with annotation
     * @return the value wrapped in an optional
     */
    public Optional<String> value(NodeWithAnnotations<?> node) {
        return node.getAnnotationByClass(annotationClass) //
                .flatMap(anno -> findByName(anno, name)) //
                .map(MemberValuePair::getValue) //
                .map(Expression::asStringLiteralExpr) //
                .map(StringLiteralExpr::asString);
    }

    private Optional<MemberValuePair> findByName(AnnotationExpr anno, String parameterName) {
        return anno.findFirst(MemberValuePair.class, mvp -> mvp.getNameAsString().equals(parameterName));
    }

}
