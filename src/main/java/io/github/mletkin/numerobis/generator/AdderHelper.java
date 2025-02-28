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

import static io.github.mletkin.numerobis.common.Util.exists;
import static io.github.mletkin.numerobis.generator.common.ClassUtil.allMember;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.fieldAccess;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.methodCall;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.methodReference;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.nameExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.returnStmt;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.thisExpr;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;

import io.github.mletkin.numerobis.generator.common.ClassUtil;
import io.github.mletkin.numerobis.generator.common.GenerationUtil;

/**
 * Generates and adds adder methods to the builder class.
 * <p>
 * An adder method
 * <ul>
 * <li>has the name "add&lt;field name&gt;" first letter of field name is
 * uppercase
 * <li>has one parameter
 * <li>returns the builder instance
 * <li>retains the original content of the field
 * </ul>
 */
public class AdderHelper {

    private BuilderGenerator owner;

    /**
     * Creates a helper instance for adder creation.
     *
     * @param owner
     *            builder generator that maintains the builder.
     */
    AdderHelper(BuilderGenerator owner) {
        this.owner = owner;
    }

    /**
     * Checks for an add method in the builder class.
     * <p>
     * signature {@code Builder addName(Type item)}
     *
     * @param amd
     *            adder descriptor
     * @return {@code true} if the method exists
     */
    void addAdder(AdderMethodDescriptor amd) {
        switch (amd.variant) {
        case ITEM:
            addItemAdder(amd);
            break;
        case STREAM:
            addStreamAdder(amd);
            break;
        case COLLECTION:
            addCollectionAdder(amd);
            break;
        case VARARG:
            addVarArgAdder(amd);
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks for an adder method in the builder class.
     * <p>
     * signature {@code Builder addName(Type item)}
     *
     * @param amd
     *            adder descriptor
     * @return {@code true} if the method exists
     */
    boolean hasAdder(AdderMethodDescriptor amd) {
        switch (amd.variant) {
        case ITEM:
        case STREAM:
        case COLLECTION:
        case VARARG:
            return hasAdderMethod(amd);
        default:
            throw new IllegalArgumentException();
        }
    }

    private boolean hasAdderMethod(AdderMethodDescriptor amd) {
        Predicate<CallableDeclaration<?>> parameterFilter = amd.variant.isVarArg() //
                ? ClassUtil.hasSingleVarArgParameter(adderParameterType(amd))
                : ClassUtil.hasSingleParameter(adderParameterType(amd));

        return exists(//
                allMember(owner.builderclass(), MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(amd.methodName)) //
                        .filter(parameterFilter) //
                        .filter(md -> md.getType().equals(owner.builderClassType())));
    }

    private void addItemAdder(AdderMethodDescriptor amd) {
        createAdder(amd, "item").createBody() // product.x.add(item)
                .addStatement(methodCall(fieldAccess(nameExpr(owner.naming().productField()), amd.fieldName), "add",
                        nameExpr("item"))) //
                .addStatement(returnStmt(thisExpr()));
    }

    private void addStreamAdder(AdderMethodDescriptor amd) {
        createAdder(amd, "items").createBody() // stream.forEach(product.x::add)
                .addStatement(methodCall(//
                        nameExpr("items"), //
                        "forEach", //
                        methodReference(fieldAccess(nameExpr(owner.naming().productField()), amd.fieldName), "add")))
                .addStatement(returnStmt(thisExpr()));
        owner.builderUnit().addImport(Stream.class);
    }

    private void addCollectionAdder(AdderMethodDescriptor amd) {
        createAdder(amd, "items").createBody() // product.x.addAll(collection)
                .addStatement(methodCall( //
                        fieldAccess(nameExpr(owner.naming().productField()), amd.fieldName), //
                        "addAll", //
                        nameExpr("items"))) //
                .addStatement(returnStmt(thisExpr()));
        owner.builderUnit().addImport(Collection.class);
    }

    private void addVarArgAdder(AdderMethodDescriptor amd) {
        createAdder(amd, "items").createBody() // Stream.of(items).forEach(product.x::add)
                .addStatement(methodCall( //
                        methodCall(nameExpr(Stream.class), "of", nameExpr("items")), //
                        "forEach", //
                        methodReference(//
                                fieldAccess(nameExpr(owner.naming().productField()), amd.fieldName), //
                                "add"))) //
                .addStatement(returnStmt(thisExpr()));
        owner.builderUnit().addImport(Stream.class);
    }

    private MethodDeclaration createAdder(AdderMethodDescriptor amd, String parameterName) {
        MethodDeclaration meth = owner.builderclass().addMethod(amd.methodName, Modifier.Keyword.PUBLIC);
        meth.addAndGetParameter(adderParameterType(amd), parameterName).setVarArgs(amd.variant.isVarArg());
        meth.setType(owner.builderClassType());
        return meth;
    }

    /**
     * Returns the paraneter type of the mutator method.
     *
     * @param amd
     *            mutator method descriptor
     * @return the parameter type
     */
    private Type adderParameterType(AdderMethodDescriptor amd) {
        switch (amd.variant) {
        case ITEM:
            return amd.parameterType;
        case STREAM:
            return GenerationUtil.streamType(amd.parameterType);
        case COLLECTION:
            return GenerationUtil.collectionType(amd.parameterType);
        case VARARG:
            return amd.parameterType;
        default:
            throw new IllegalArgumentException();
        }
    }

}
