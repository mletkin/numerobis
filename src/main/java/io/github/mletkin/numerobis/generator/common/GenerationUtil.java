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

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

/**
 * Functions for the generation of Java expressions and statements.
 */
public final class GenerationUtil {

    private GenerationUtil() {
        // prevent instantiation
    }

    /**
     * Creates an assign expression object.
     *
     * @param  field the field that gets assigned
     * @param  value the value that is assigned
     * @return       the {@link AssignExpr} instance
     */
    public static AssignExpr assignExpr(String field, Expression value) {
        return assignExpr(new NameExpr(field), value);
    }

    /**
     * Creates an assign expression object.
     *
     * @param  left  expression the expression that gets assigned
     * @param  value expression the expression that is assigned
     * @return       the {@link AssignExpr} instance
     */
    public static AssignExpr assignExpr(Expression left, Expression value) {
        return new AssignExpr(left, value, AssignExpr.Operator.ASSIGN);
    }

    /**
     * Creates a object generation expression.
     *
     * @param  type the type of the new instance
     * @param  args arguments for the constructor
     * @return      the {@link ObjectCreationExpr} instance
     */
    public static ObjectCreationExpr newExpr(ClassOrInterfaceType type, Expression... args) {
        return new ObjectCreationExpr(null, type, new NodeList<>(args));
    }

    /**
     * Creates an expression fom a name.
     *
     * @param  name name to use
     * @return      the name expression
     */
    public static NameExpr nameExpr(String name) {
        return new NameExpr(name);
    }

    /**
     * Creates an expression fom the simple name of a class.
     *
     * @param  clazz class object
     * @return       the name expression
     */
    public static NameExpr nameExpr(Class<?> clazz) {
        return new NameExpr(clazz.getSimpleName());
    }

    /**
     * Creates a return statement.
     *
     * @param  value the expression the statement returns.
     * @return       the {@link ReturnStmt}
     */
    public static ReturnStmt returnStmt(Expression value) {
        return new ReturnStmt(value);
    }

    /**
     * Returns a list of expressions describing the constructor parameters.
     *
     * @param  constructor constructor deplaration to process
     * @return             array of {@link Expression} instances
     */
    public static Expression[] args(ConstructorDeclaration constructor) {
        return constructor.getParameters().stream() //
                .map(Parameter::getNameAsString) //
                .map(NameExpr::new) //
                .toArray(Expression[]::new);
    }

    /**
     * Returns a method call statement.
     *
     * @param  scope expression that evaluates to an object reference
     * @param  name  name of the Method
     * @param  args  list of arguments for the call
     * @return       the {@link MethodCallExpr} instance
     */
    public static MethodCallExpr methodCall(Expression scope, String name, Expression... args) {
        return new MethodCallExpr(scope, name, new NodeList<>(args));
    }

    /**
     * Creates an expression representing a method reference.
     *
     * @param  scope expression that evaluates to an object reference
     * @param  name  method name to call
     * @return       method reference expression (like {@code dings:: bums})
     */
    public static MethodReferenceExpr methodReference(Expression scope, String name) {
        return new MethodReferenceExpr(scope, null, name);
    }

    /**
     * Creates an expression refering to the object.
     *
     * @return the {@link ThisExpr} instance
     */
    public static ThisExpr thisExpr() {
        return new ThisExpr();
    }

    /**
     * Creates an expression for the access of an object's field.
     *
     * @param  scope     expression that evaluates to an object reference
     * @param  fieldName name of the field
     * @return
     */
    public static FieldAccessExpr fieldAccess(Expression scope, String fieldName) {
        return new FieldAccessExpr(scope, fieldName);
    }

    /**
     * Creates a public member class.
     *
     * @param  className name of the new class
     * @return           class declaration
     */
    public static ClassOrInterfaceDeclaration newMemberClass(String className) {
        return new ClassOrInterfaceDeclaration(new NodeList<>(Modifier.publicModifier(), Modifier.staticModifier()),
                false, className);

    }

    /**
     * Returns the {@code Stream} type for a {@code Argument} type.
     *
     * @param  argumentType argument type for the stream
     * @return              the stream type
     */
    public static Type streamType(Type argumentType) {
        return new ClassOrInterfaceType() //
                .setName("Stream") //
                .setTypeArguments(argumentType);
    }

    /**
     * Returns the {@code Collection} type for a {@code Argument} type.
     *
     * @param  argumentType argument type for the stream
     * @return              the stream type
     */
    public static Type collectionType(Type argumentType) {
        return new ClassOrInterfaceType() //
                .setName("Collection") //
                .setTypeArguments(argumentType);
    }

}
