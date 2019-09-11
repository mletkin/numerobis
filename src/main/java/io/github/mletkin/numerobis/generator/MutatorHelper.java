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
import static io.github.mletkin.numerobis.generator.common.ClassUtil.firstTypeArgument;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.assignExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.collectionType;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.fieldAccess;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.methodCall;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.nameExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.returnStmt;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.streamType;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.thisExpr;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;

import io.github.mletkin.numerobis.generator.common.ClassUtil;
import io.github.mletkin.numerobis.generator.mutator.MutatorMethodDescriptor;

/**
 * Helper class for generation of mutator methods.
 * <p>
 * A mutator method
 * <ul>
 * <li>has the name "with&lt;field name&gt;" first letter of field name is
 * uppercase
 * <li>has one parameter
 * <li>returns the builder instance
 * <li>replaces the original content of the field
 * </ul>
 */
public class MutatorHelper {

    private BuilderGenerator owner;

    /**
     * Creates a helper instance for mutator creation.
     *
     * @param owner
     *            builder generator that maintains the builder.
     */
    MutatorHelper(BuilderGenerator owner) {
        this.owner = owner;
    }

    /**
     * Adds a mutator to the builder class as described.
     *
     * @param mmd
     *            mutator method descriptor
     */
    void addMutator(MutatorMethodDescriptor mmd) {
        switch (mmd.variant()) {
        case OBJECT:
            addObjectMutator(mmd);
            break;
        case STREAM:
            addStreamMutator(mmd);
            break;
        case COLLECTION:
            addCollectionMutator(mmd);
            break;
        case VARARG:
            addVarArgMutator(mmd);
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks, whether the builder has a given mutator.
     *
     * @param mmd
     *            mutator method descriptor
     * @return {@code true} if a mutator matching the signature exists
     */
    boolean hasMutator(MutatorMethodDescriptor mmd) {
        switch (mmd.variant()) {
        case OBJECT:
        case STREAM:
        case COLLECTION:
        case VARARG:
            return hasMutatorMethod(mmd);
        default:
            throw new IllegalArgumentException();
        }
    }

    private boolean hasMutatorMethod(MutatorMethodDescriptor mmd) {
        Predicate<CallableDeclaration<?>> parameterFilter = mmd.variant().isVarArg() //
                ? ClassUtil.hasSingleVarArgParameter(mutatorParameterType(mmd))
                : ClassUtil.hasSingleParameter(mutatorParameterType(mmd));

        return exists(//
                allMember(owner.builderClass(), MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(mmd.methodName())) //
                        .filter(parameterFilter) //
                        .filter(md -> md.getType().equals(owner.builderClassType())));
    }

    private void addObjectMutator(MutatorMethodDescriptor mmd) {
        createMethod(mmd, mmd.parameterName()).createBody() // product.x = x
                .addStatement(assignExpr(fieldAccess(nameExpr(owner.naming().productField()), mmd.parameterName()),
                        nameExpr(mmd.parameterName()))) //
                .addStatement(returnStmt(thisExpr()));
    }

    private void addStreamMutator(MutatorMethodDescriptor mmd) {
        createMethod(mmd, "items").createBody() // product.x = items.collect(Collectors.toList())
                .addStatement(assignExpr(//
                        fieldAccess(nameExpr(owner.naming().productField()), mmd.parameterName()), //
                        methodCall(//
                                nameExpr("items"), //
                                "collect", //
                                methodCall(nameExpr(Collectors.class), collector(mmd))))) //
                .addStatement(returnStmt(thisExpr()));

        owner.builderUnit().addImport(Stream.class);
        owner.builderUnit().addImport(Collectors.class);
    }

    private String collector(MutatorMethodDescriptor mmd) {
        if (ClassUtil.extendsInterface(mmd.parameterType(), List.class, owner.productUnit())) {
            return "toList";
        }
        if (ClassUtil.extendsInterface(mmd.parameterType(), Set.class, owner.productUnit())) {
            return "toSet";
        }
        throw new IllegalArgumentException();
    }

    private void addCollectionMutator(MutatorMethodDescriptor mmd) {
        createMethod(mmd, "items").createBody() // product.x = items.stream().collect(Collectors.toList())
                .addStatement(assignExpr(//
                        fieldAccess(nameExpr(owner.naming().productField()), mmd.parameterName()), //
                        methodCall(//
                                methodCall(nameExpr("items"), "stream"), //
                                "collect", //
                                methodCall(nameExpr(Collectors.class), collector(mmd))))) //
                .addStatement(returnStmt(thisExpr()));

        owner.builderUnit().addImport(Collectors.class);
        owner.builderUnit().addImport(Collection.class);
    }

    private void addVarArgMutator(MutatorMethodDescriptor mmd) {
        createMethod(mmd, "items").createBody() // product.x = Stream.of(items).collect(Collectors.toList())
                .addStatement(assignExpr(//
                        fieldAccess(nameExpr(owner.naming().productField()), mmd.parameterName()), //
                        methodCall(//
                                methodCall(nameExpr(Stream.class), "of", nameExpr("items")), //
                                "collect", //
                                methodCall(nameExpr(Collectors.class), collector(mmd))))) //
                .addStatement(returnStmt(thisExpr()));

        owner.builderUnit().addImport(Stream.class);
        owner.builderUnit().addImport(Collectors.class);
    }

    private MethodDeclaration createMethod(MutatorMethodDescriptor mmd, String parameterName) {
        MethodDeclaration meth = owner.builderclass().addMethod(mmd.methodName(), Modifier.Keyword.PUBLIC);
        meth.addAndGetParameter(mutatorParameterType(mmd), parameterName) //
                .setVarArgs(mmd.variant().isVarArg());
        meth.setType(owner.builderClassType());
        return meth;
    }

    /**
     * Returns the parameter type of a mutator method.
     *
     * @param mmd
     *            mutator method descriptor
     * @return the parameter type
     */
    private Type mutatorParameterType(MutatorMethodDescriptor mmd) {
        switch (mmd.variant()) {
        case OBJECT:
            return mmd.parameterType();
        case STREAM:
            return streamType(firstTypeArgument(mmd.parameterType()));
        case COLLECTION:
            return collectionType(firstTypeArgument(mmd.parameterType()));
        case VARARG:
            return firstTypeArgument(mmd.parameterType());
        default:
            throw new IllegalArgumentException();
        }
    }

}
