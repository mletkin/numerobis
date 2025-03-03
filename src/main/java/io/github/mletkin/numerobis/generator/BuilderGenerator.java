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
import static io.github.mletkin.numerobis.generator.common.ClassUtil.hasDefaultConstructor;
import static io.github.mletkin.numerobis.generator.common.ClassUtil.hasExplicitConstructor;
import static io.github.mletkin.numerobis.generator.common.ClassUtil.hasProductConstructor;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.args;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.assignExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.fieldAccess;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.nameExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.newExpr;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.returnStmt;
import static io.github.mletkin.numerobis.generator.common.GenerationUtil.thisExpr;
import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import io.github.mletkin.numerobis.annotation.Ignore;
import io.github.mletkin.numerobis.annotation.Immutable;
import io.github.mletkin.numerobis.annotation.Mutable;
import io.github.mletkin.numerobis.common.VisibleForTesting;
import io.github.mletkin.numerobis.generator.common.ClassUtil;
import io.github.mletkin.numerobis.generator.mutator.ListMutatorDescriptorGenerator;
import io.github.mletkin.numerobis.generator.mutator.MutatorDescriptorGenerator;
import io.github.mletkin.numerobis.generator.mutator.MutatorMethodDescriptor;
import io.github.mletkin.numerobis.plugin.Naming;

/**
 * Generates builder classes for product classes.
 */
public class BuilderGenerator {

    private boolean separateClass = true;
    private boolean mutableByDefault = false;

    private CompilationUnit productUnit;
    private ClassOrInterfaceDeclaration productclass;

    private Forge forge;

    private AdderHelper adderHelper = new AdderHelper(this);
    private MutatorHelper mutatorHelper = new MutatorHelper(this);
    private Naming naming = Naming.DEFAULT;

    /**
     * Creates a generator for the builder class.
     *
     * @param productUnit      the unit with the product class definition
     * @param productClassName the name of the product class
     */
    public BuilderGenerator(CompilationUnit productUnit, String productClassName) {
        this.productUnit = productUnit;
        this.productclass = ClassUtil.findClass(productUnit, productClassName).orElse(null);

        ifNotThrow(productclass != null, () -> GeneratorException.productClassNotFound(productClassName));
        ifNotThrow(hasUsableConstructor(productclass), GeneratorException::noConstructorFound);
    }

    /**
     * Sets the default mutability flag.
     * <p>
     * The value might be overriden by an annotation
     *
     * @param  mutableByDefault the default value to set
     * @return                  the {@code BuilderGenerator} instance
     */
    public BuilderGenerator mutableByDefault(boolean mutableByDefault) {
        this.mutableByDefault = mutableByDefault;
        return this;
    }

    /**
     * Sets the naming settings to use.
     *
     * @param  naming object with naming settings
     * @return        the {@code BuilderGenerator} instance
     */
    public BuilderGenerator withNamingSettings(Naming naming) {
        this.naming = naming;
        return this;
    }

    /**
     * Creates a generator for an embedded builder class.
     *
     * @return the {@code BuilderGenerator} instance
     */
    public BuilderGenerator withInternalBuilder() {
        this.forge = Forge.internal(productUnit, productclass, naming.builderClassPostfix());
        separateClass = false;
        return this;
    }

    /**
     * Creates a generator for a separate builder class.
     *
     * @param  builderUnit the unit that takes the builder
     * @return             the {@code BuilderGenerator} instance
     */
    public BuilderGenerator withExternalBuilder(CompilationUnit builderUnit) {
        this.forge = Forge.external(builderUnit, productClassName(), naming.builderClassPostfix());
        productUnit.getPackageDeclaration().ifPresent(forge::setPackageDeclaration);
        forge.copyImports(productUnit);
        return this;
    }

    /**
     * Adds a field for the product to the builder.
     *
     * @return the {@code BuilderGenerator} instance
     */
    public BuilderGenerator addProductField() {
        if (!hasProductField()) {
            builderclass().addField(productClassName(), naming.productField(), Modifier.Keyword.PRIVATE);
        }
        return this;
    }

    private boolean hasProductField() {
        var productField = findProductField();
        productField.filter(vd -> !vd.getType().equals(productClassType())).ifPresent(type -> {
            throw GeneratorException.productFieldHasWrongType(type);
        });
        return productField.isPresent();
    }

    private Optional<VariableDeclarator> findProductField() {
        return allMember(builderclass(), FieldDeclaration.class) //
                .map(FieldDeclaration::getVariables) //
                .flatMap(List::stream) //
                .filter(vd -> vd.getNameAsString().equals(naming.productField())) //
                .findFirst();
    }

    /**
     * Adds a builder constructor for each constructor in the product class.
     *
     * @return the {@code BuilderGenerator} instance
     */
    public BuilderGenerator addConstructors() {
        if (!hasExplicitConstructor(productclass) && !hasDefaultConstructor(builderclass())) {
            addDefaultConstructor();
        }
        allMember(productclass, ConstructorDeclaration.class) //
                .filter(this::process) //
                .filter(not(forge::hasMatchingConstructor)) //
                .forEach(this::addMatchingConstructor);
        if (isProductMutable() && !hasManipulationConstructor()) {
            addManipulationConstructor();
        }
        return this;
    }

    private boolean hasManipulationConstructor() {
        return exists(//
                allMember(builderclass(), ConstructorDeclaration.class) //
                        .filter(ClassUtil.hasSingleParameter(productClassType())));
    }

    private void addManipulationConstructor() {
        builderclass().addConstructor(Modifier.Keyword.PUBLIC) //
                .addParameter(productClassType(), naming.productField()) //
                .createBody() //
                .addStatement(
                        assignExpr(fieldAccess(thisExpr(), naming.productField()), nameExpr(naming.productField())));
    }

    private boolean process(ConstructorDeclaration cd) {
        if (cd.isAnnotationPresent(Ignore.class)) {
            return false;
        }
        if (cd.isPrivate() && separateClass) {
            return false;
        }
        return true;
    }

    private void addDefaultConstructor() {
        builderclass().addConstructor(Modifier.Keyword.PUBLIC) //
                .createBody() //
                .addStatement(assignExpr(naming.productField(), newExpr(productClassType())));
    }

    private void addMatchingConstructor(ConstructorDeclaration productConstructor) {
        ConstructorDeclaration builderconstructor = builderclass().addConstructor(Modifier.Keyword.PUBLIC);
        productConstructor.getParameters().stream().forEach(builderconstructor::addParameter);
        builderconstructor.createBody() //
                .addStatement(assignExpr(naming.productField(), newExpr(productClassType(), args(productConstructor))));
    }

    /**
     * Adds a builder factory method for each product constructor.
     *
     * @return the {@code BuilderGenerator} instance
     */
    public BuilderGenerator addFactoryMethods() {
        if (!hasProductConstructor(builderclass(), productClassName())) {
            addProductConstructor();
        }
        if (!hasExplicitConstructor(productclass) && !hasDefaultFactoryMethod()) {
            addDefaultFactoryMethod();
        }
        if (isProductMutable() && !hasManipulationFactoryMethod()) {
            addManipulationFactoryMethod();
        }
        allMember(productclass, ConstructorDeclaration.class) //
                .filter(this::process) //
                .filter(not(this::hasMatchingFactoryMethod)) //
                .forEach(this::addFactoryMethod);

        return this;
    }

    private boolean hasManipulationFactoryMethod() {
        return exists(//
                allMember(builderclass(), MethodDeclaration.class) //
                        .filter(MethodDeclaration::isStatic) //
                        .filter(md -> md.getNameAsString().equals(naming.factoryMethod())) //
                        .filter(md -> md.getTypeAsString().equals(builderClassName())) //
                        .filter(ClassUtil.hasSingleParameter(productClassType())));
    }

    private void addManipulationFactoryMethod() {
        MethodDeclaration factoryMethod = //
                builderclass().addMethod(naming.factoryMethod(), Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        factoryMethod.setType(builderClassName());
        factoryMethod.addParameter(productClassName(), naming.productField());
        factoryMethod.createBody() //
                .addStatement(returnStmt(newExpr(builderClassType(), nameExpr(naming.productField()))));
    }

    /**
     * Adds a default factory method to the builder class.
     * <p>
     * signature: {@code public static Builder of();}
     */
    private void addDefaultFactoryMethod() {
        MethodDeclaration factoryMethod = //
                builderclass().addMethod(naming.factoryMethod(), Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        factoryMethod.setType(builderClassName());
        factoryMethod.createBody() //
                .addStatement(returnStmt(newExpr(builderClassType(), newExpr(productClassType()))));
    }

    private boolean hasDefaultFactoryMethod() {
        return exists(//
                allMember(builderclass(), MethodDeclaration.class) //
                        .filter(MethodDeclaration::isStatic) //
                        .filter(md -> md.getNameAsString().equals(naming.factoryMethod())) //
                        .filter(md -> md.getTypeAsString().equals(builderClassName())) //
                        .filter(md -> md.getParameters().isEmpty()));
    }

    /**
     * Adds a product constructor to the builder.
     * <p>
     * signature {@code private Builder(Product product)}
     */
    private void addProductConstructor() {
        ConstructorDeclaration constructor = builderclass().addConstructor(Modifier.Keyword.PRIVATE);
        constructor.addParameter(productClassName(), naming.productField());
        constructor.createBody() //
                .addStatement(
                        assignExpr(fieldAccess(thisExpr(), naming.productField()), nameExpr(naming.productField())));
    }

    private void addFactoryMethod(ConstructorDeclaration productConstructor) {
        MethodDeclaration factoryMethod = //
                builderclass().addMethod(naming.factoryMethod(), Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        productConstructor.getParameters().stream().forEach(factoryMethod::addParameter);
        factoryMethod.setType(builderClassName());
        factoryMethod.createBody() //
                .addStatement(//
                        returnStmt(newExpr(builderClassType(), newExpr(productClassType(), args(productConstructor)))));
    }

    private boolean hasMatchingFactoryMethod(ConstructorDeclaration productConstructor) {
        return exists( //
                allMember(builderclass(), MethodDeclaration.class) //
                        .filter(MethodDeclaration::isStatic) //
                        .filter(md -> md.getNameAsString().equals(naming.factoryMethod())) //
                        .filter(md -> md.getTypeAsString().equals(builderClassName())) //
                        .filter(md -> ClassUtil.matchesParameter(md, productConstructor)));
    }

    /**
     * Adds a mutator for each field of the product.
     *
     * @param  mutatorVariants list of variants to generate
     * @return                 the {@code BuilderGenerator} instance
     */
    public BuilderGenerator addMutator(ListMutatorVariant[] mutatorVariants) {
        allMember(productclass, FieldDeclaration.class) //
                .filter(this::process) //
                .flatMap(fd -> mutatorDescriptors(mutatorVariants, fd)) //
                .filter(not(mutatorHelper::hasMutator)) //
                .forEach(mutatorHelper::addMutator);
        return this;
    }

    private Stream<MutatorMethodDescriptor> mutatorDescriptors(ListMutatorVariant[] mutatorVariants,
            FieldDeclaration fd) {
        return ClassUtil.isCollection(fd, productUnit) //
                ? new ListMutatorDescriptorGenerator(fd, mutatorVariants, naming.mutatorPrefix()).stream()
                : new MutatorDescriptorGenerator(fd, naming.mutatorPrefix()).stream();
    }

    private boolean process(FieldDeclaration fd) {
        if (fd.isAnnotationPresent(Ignore.class)) {
            return false;
        }
        if (fd.isPrivate() && separateClass) {
            return false;
        }
        return true;
    }

    /**
     * Adds the build method to the builder class.
     *
     * @return the {@code BuilderGenerator} instance
     */
    public BuilderGenerator addBuildMethod() {
        if (!hasBuildMethod()) {
            builderclass().addMethod(naming.buildMethod(), Modifier.Keyword.PUBLIC) //
                    .setType(productClassType()) //
                    .createBody() //
                    .addStatement(returnStmt(nameExpr(naming.productField())));
        }
        return this;
    }

    private boolean hasBuildMethod() {
        return exists( //
                allMember(builderclass(), MethodDeclaration.class) //
                        .filter(md -> md.getNameAsString().equals(naming.buildMethod())) //
                        .filter(md -> md.getType().equals(productClassType())));
    }

    /**
     * Adds an adder method for each list implementing field in the product.
     *
     * @param  adderVariants list of variants to generate
     * @return               the {@code BuilderGenerator} instance
     */
    public BuilderGenerator addAdder(ListMutatorVariant[] adderVariants) {
        allMember(productclass, FieldDeclaration.class) //
                .filter(this::process) //
                .flatMap(fd -> new AdderMethodDescriptor.Generator(fd, adderVariants, productUnit, naming.adderPrefix())
                        .stream()) //
                .filter(not(adderHelper::hasAdder)) //
                .forEach(adderHelper::addAdder);
        return this;
    }

    /**
     * Returns the type of the builder class.
     *
     * @return the {@link ClassOrInterfaceType} object
     */
    public ClassOrInterfaceType builderClassType() {
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

    /**
     * Returns the builder class declaration.
     *
     * @return the {@link ClassOrInterfaceDeclaration} object
     */
    public ClassOrInterfaceDeclaration builderclass() {
        return forge.builderClass();
    }

    /**
     * Returns the compilation unit containig the builder class.
     *
     * @return the {@link CompilationUnit} object
     */
    public CompilationUnit builderUnit() {
        return forge.builderUnit();
    }

    /**
     * Checks a class declaration for a usable constructor.
     * <p>
     * The constructor must be callable by the builder
     *
     * @param  type class to check
     * @return      {@code true} when the class contains a fitting constructor.
     */
    private boolean hasUsableConstructor(ClassOrInterfaceDeclaration type) {
        List<ConstructorDeclaration> constructorList = //
                allMember(type, ConstructorDeclaration.class).collect(Collectors.toList());

        return constructorList.isEmpty() || constructorList.stream().anyMatch(this::process);
    }

    /**
     * Checks if the product class should be considered mutable.
     *
     * @return {@code true} when the class should be mutable
     */
    @VisibleForTesting
    boolean isProductMutable() {
        return productclass.isAnnotationPresent(Mutable.class)
                || (mutableByDefault && !productclass.isAnnotationPresent(Immutable.class));
    }

    /**
     * Returns the compilation unit containing the product class.
     *
     * @return the {@link CompilationUnit} object
     */
    public CompilationUnit productUnit() {
        return productUnit;
    }

    /**
     * Returns the naimng settings of the forge.
     *
     * @return the {@link Naming} object
     */
    public Naming naming() {
        return naming;
    }

}
