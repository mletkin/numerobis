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

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

/**
 * Functions for the generation of Java expressions and statements.
 */
public final class GenerationUtil {

    private GenerationUtil() {
        // prevent instantiation
    }

    static AssignExpr assignExpr(String field, Expression value) {
        return assignExpr(new NameExpr(field), value);
    }

    static AssignExpr assignExpr(Expression left, Expression value) {
        return new AssignExpr(left, value, AssignExpr.Operator.ASSIGN);
    }

    static ObjectCreationExpr newExpr(ClassOrInterfaceType type, Expression... args) {
        return new ObjectCreationExpr(null, type, new NodeList<>(args));
    }

    static NameExpr nameExpr(String name) {
        return new NameExpr(name);
    }

    static ReturnStmt returnStmt(Expression value) {
        return new ReturnStmt(value);
    }

    static Expression[] args(ConstructorDeclaration constructor) {
        return constructor.getParameters().stream() //
                .map(Parameter::getNameAsString) //
                .map(NameExpr::new) //
                .toArray(Expression[]::new);
    }

    static MethodCallExpr methodCall(Expression scope, String name, Expression... args) {
        return new MethodCallExpr(scope, name, new NodeList<>(args));
    }

    static ThisExpr thisExpr() {
        return new ThisExpr();
    }

    static FieldAccessExpr fieldAccess(Expression scope, String fieldName) {
        return new FieldAccessExpr(scope, fieldName);
    }

    /**
     * Creates a public member class.
     *
     * @param className
     *            name of the new class
     * @return class declaration
     */
    static ClassOrInterfaceDeclaration newMemberClass(String className) {
        return new ClassOrInterfaceDeclaration(new NodeList<>(Modifier.publicModifier(), Modifier.staticModifier()),
                false, className);

    }
}
