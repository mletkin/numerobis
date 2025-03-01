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
import static io.github.mletkin.numerobis.common.Util.ifNotThrow;
import static io.github.mletkin.numerobis.generator.common.ClassUtil.allMember;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.assignExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.fieldAccess;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.nameExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.newExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.returnStmt;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.thisExpr;
import static java.util.function.Predicate.not;

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import io.github.mletkin.numerobis.common.Util;
import io.github.mletkin.numerobis.generator.common.ClassUtil;
import io.github.mletkin.numerobis.plugin.Naming;

/**
 * Generates a builder class for a record product.
 * <ul>
 * <li>records are immutable
 * <li>no factory methods are generated
 * </ul>
 */
public class RecordBuilderGenerator {

    private Naming naming = Naming.DEFAULT;
    private CompilationUnit productUnit;
    private RecordDeclaration productclass;
    private Forge forge;

    /**
     * Creates a generator for the builder class.
     *
     * @param productUnit       unit with the product class definition
     * @param productRecordName name of the product record
     */
    public RecordBuilderGenerator(CompilationUnit productUnit, String productRecordName) {
        this.productUnit = productUnit;
        this.productclass = ClassUtil.findRecord(productUnit, productRecordName).orElse(null);

        ifNotThrow(productclass != null, () -> GeneratorException.productClassNotFound(productRecordName));
    }

    public RecordBuilderGenerator withNamingSettings(Naming naming) {
        this.naming = naming;
        return this;
    }

    /**
     * Create a generator for an embedded builder class.
     *
     * @return The {@code BuilderGenerator}
     */
    public RecordBuilderGenerator withInternalBuilder() {
        this.forge = Forge.internal(productUnit, productclass, naming.builderClassPostfix());
        return this;
    }

    /**
     * Create a generator for a separate builder class.
     * <p>
     * The builder class might already exist
     *
     * @param  builderUnit the unit to contain the builder
     * @return             The {@code BuilderGenerator}
     */
    public RecordBuilderGenerator withExternalBuilder(CompilationUnit builderUnit) {
        this.forge = Forge.external(builderUnit, productClassName(), naming.builderClassPostfix());
        productUnit.getPackageDeclaration().ifPresent(forge::setPackageDeclaration);
        forge.copyImports(productUnit);
        return this;
    }

    /**
     * Add a builder field for every record parameter.
     */
    public RecordBuilderGenerator addFields() {
        productclass.getParameters().stream() //
                .filter(not(this::hasField)) //
                .forEach(this::addField);
        return this;
    }

    private boolean hasField(Parameter para) {
        return allMember(builderclass(), FieldDeclaration.class) //
                .map(FieldDeclaration::getVariables) //
                .flatMap(List::stream) //
                .anyMatch(vd -> vd.getNameAsString().equals(para.getNameAsString()));
    }

    private void addField(Parameter para) {
        builderclass().addField(para.getType(), para.getNameAsString(), Keyword.PRIVATE);
    }

    /**
     * Add a mutator for every builder field / record parameter.
     */
    public RecordBuilderGenerator addMutators() {
        productclass.getParameters().stream() //
                .filter(not(this::hasMutator)) //
                .forEach(this::addMutator);
        return this;
    }

    private boolean hasMutator(Parameter para) {
        return exists(//
                allMember(builderclass(), MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(mutatorName(para.getNameAsString()))) //
                        .filter(ClassUtil.hasSingleParameter(para.getType())) //
                        .filter(md -> md.getType().equals(builderClassType())));
    }

    private void addMutator(Parameter para) {
        var fieldName = para.getNameAsString();

        createMutatorDeclaration(fieldName, para) //
                .createBody() //
                .addStatement(assignExpr(fieldAccess(thisExpr(), fieldName), nameExpr(fieldName))) //
                .addStatement(returnStmt(thisExpr()));
    }

    private MethodDeclaration createMutatorDeclaration(String fieldName, Parameter para) {
        return builderclass() //
                .addMethod(mutatorName(fieldName), Modifier.Keyword.PUBLIC) //
                .addParameter(para.getType(), para.getNameAsString()) //
                .setType(builderClassType());
    }

    private String mutatorName(String fieldName) {
        return naming.mutatorPrefix() + Util.firstLetterUppercase(fieldName);
    }

    /**
     * Adds a default builder constructor.
     *
     * @return the generator instance
     */
    public RecordBuilderGenerator addConstructors() {
        addDefaultConstructor();
        return this;
    }

    private void addDefaultConstructor() {
        builderclass().addConstructor(Modifier.Keyword.PUBLIC) //
                .createBody() //
                .addStatement(assignExpr(naming.productField(), newExpr(productClassType())));
    }

    /**
     * Adds the build method to the builder class.
     *
     * @return the generator instance
     */
    public RecordBuilderGenerator addBuildMethod() {
        if (!hasBuildMethod()) {
            builderclass().addMethod(naming.buildMethod(), Modifier.Keyword.PUBLIC) //
                    .setType(productClassType()) //
                    .createBody() //
                    .addStatement(returnStmt(mkRecordInstance()));
        }
        return this;
    }

    private ObjectCreationExpr mkRecordInstance() {
        var fields = productclass.getParameters().stream() //
                .map(Parameter::getNameAsString) //
                .map(NameExpr::new) //
                .toArray(NameExpr[]::new);

        return newExpr(productClassType(), fields);
    }

    private boolean hasBuildMethod() {
        return exists( //
                allMember(builderclass(), MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(naming.buildMethod())) //
                        .filter(md -> md.getType().equals(productClassType())));
    }

    private ClassOrInterfaceType builderClassType() {
        return new ClassOrInterfaceType(builderClassName());
    }

    private String productClassName() {
        return productclass.getNameAsString();
    }

    private ClassOrInterfaceType productClassType() {
        return new ClassOrInterfaceType(productClassName());
    }

    private String builderClassName() {
        return builderclass().getNameAsString();
    }

    private ClassOrInterfaceDeclaration builderclass() {
        return forge.builderClass();
    }

    public CompilationUnit builderUnit() {
        return forge.builderUnit();
    }

}
