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
import static io.github.mletkin.numerobis.generator.ClassUtil.allMember;
import static io.github.mletkin.numerobis.generator.ClassUtil.hasSingleVarArgParameter;
import static io.github.mletkin.numerobis.generator.GenerationUtil.fieldAccess;
import static io.github.mletkin.numerobis.generator.GenerationUtil.methodCall;
import static io.github.mletkin.numerobis.generator.GenerationUtil.methodReference;
import static io.github.mletkin.numerobis.generator.GenerationUtil.nameExpr;
import static io.github.mletkin.numerobis.generator.GenerationUtil.returnStmt;
import static io.github.mletkin.numerobis.generator.GenerationUtil.thisExpr;

import java.util.Collection;
import java.util.stream.Stream;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Generates and adds adder methods to the builder class.
 * <p>
 * Contains Methods extracted from the builder generater to reduce class size.
 * <p>
 * FIXME: add imports
 */
public class AdderHelper {

    BuilderGenerator owner;

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
    void addAdderMethod(AdderMethodDescriptor amd) {
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
     * Checks for an add method in the builder class.
     * <p>
     * signature {@code Builder addName(Type item)}
     *
     * @param amd
     *            adder descriptor
     * @return {@code true} if the method exists
     */
    boolean hasAdderMethod(AdderMethodDescriptor amd) {
        switch (amd.variant) {
        case ITEM:
            return hasItemAdder(amd);
        case STREAM:
            return hasStreamAdder(amd);
        case COLLECTION:
            return hasCollectionAdder(amd);
        case VARARG:
            return hasVarArgAdder(amd);
        default:
            throw new IllegalArgumentException();
        }
    }

    private boolean hasItemAdder(AdderMethodDescriptor amd) {
        return exists(//
                allMember(owner.builderclass, MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(amd.methodName)) //
                        .filter(ClassUtil.hasSingleParameter(amd.parameterType)) //
                        .filter(md -> md.getType().equals(owner.builderClassType())));
    }

    private void addItemAdder(AdderMethodDescriptor amd) {
        MethodDeclaration meth = owner.builderclass.addMethod(amd.methodName, Modifier.Keyword.PUBLIC);
        meth.addParameter(amd.parameterType, "item");
        meth.setType(owner.builderClassType());
        meth.createBody() //
                .addStatement(methodCall(fieldAccess(nameExpr(BuilderGenerator.FIELD), amd.fieldName), "add",
                        nameExpr("item"))) //
                .addStatement(returnStmt(thisExpr()));
    }

    private boolean hasStreamAdder(AdderMethodDescriptor amd) {
        return exists(//
                allMember(owner.builderclass, MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(amd.methodName)) //
                        .filter(ClassUtil.hasSingleParameter(GenerationUtil.streamType(amd.parameterType))) //
                        .filter(md -> md.getType().equals(owner.builderClassType())));
    }

    private void addStreamAdder(AdderMethodDescriptor amd) {
        MethodDeclaration meth = owner.builderclass.addMethod(amd.methodName, Modifier.Keyword.PUBLIC);
        meth.addParameter(GenerationUtil.streamType(amd.parameterType), "stream");
        meth.setType(owner.builderClassType());
        meth.createBody() //
                .addStatement(methodCall(//
                        nameExpr("stream"), //
                        "forEach", //
                        methodReference(fieldAccess(nameExpr(BuilderGenerator.FIELD), amd.fieldName), "add")))
                .addStatement(returnStmt(thisExpr()));
        owner.builderUnit().addImport(Stream.class);
    }

    private boolean hasCollectionAdder(AdderMethodDescriptor amd) {
        return exists(//
                allMember(owner.builderclass, MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(amd.methodName)) //
                        .filter(ClassUtil.hasSingleParameter(GenerationUtil.collectionType(amd.parameterType))) //
                        .filter(md -> md.getType().equals(owner.builderClassType())));
    }

    private void addCollectionAdder(AdderMethodDescriptor amd) {
        MethodDeclaration meth = owner.builderclass.addMethod(amd.methodName, Modifier.Keyword.PUBLIC);
        meth.addParameter(GenerationUtil.collectionType(amd.parameterType), "collection");
        meth.setType(owner.builderClassType()); //
        meth.createBody() //
                .addStatement(methodCall( //
                        fieldAccess(nameExpr(BuilderGenerator.FIELD), amd.fieldName), //
                        "addAll", //
                        nameExpr("collection"))) //
                .addStatement(returnStmt(thisExpr()));
        owner.builderUnit().addImport(Collection.class);
    }

    private boolean hasVarArgAdder(AdderMethodDescriptor amd) {
        return exists(//
                allMember(owner.builderclass, MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(amd.methodName)) //
                        .filter(hasSingleVarArgParameter(amd.parameterType)) //
                        .filter(md -> md.getType().equals(owner.builderClassType())));
    }

    private void addVarArgAdder(AdderMethodDescriptor amd) {
        MethodDeclaration meth = owner.builderclass.addMethod(amd.methodName, Modifier.Keyword.PUBLIC);
        meth.addAndGetParameter(amd.parameterType, "items").setVarArgs(true);
        meth.setType(owner.builderClassType()); //
        meth.createBody() //
                .addStatement(methodCall( //
                        methodCall(nameExpr(Stream.class), "of", nameExpr("items")), //
                        "forEach", //
                        methodReference(//
                                fieldAccess(nameExpr(BuilderGenerator.FIELD), amd.fieldName), //
                                "add"))) //
                .addStatement(returnStmt(thisExpr()));
        owner.builderUnit().addImport(Stream.class);
    }

}
