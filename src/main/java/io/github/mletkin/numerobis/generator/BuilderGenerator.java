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

import static io.github.mletkin.numerobis.generator.ProductUtil.hasExplicitConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import io.github.mletkin.numerobis.common.Util;

/**
 * Generates builder classes product classes in a seperate compilation unit.
 */
public class BuilderGenerator {

    private static final String BUILDER_PACKAGE = "io.github.mletkin.numerobis";
    private final static String FIELD = "product";
    private final static String CLASS_POSTFIX = "Builder";

    final static String BUILD_METHOD = "build";
    final static String FACTORY_METHOD = "of";
    final static String WITH_PREFIX = "with";
    final static String ADD_PREFIX = "add";

    private CompilationUnit productUnit;
    private String productClassName;
    private CompilationUnit builderUnit;
    private ClassOrInterfaceDeclaration builderclass;

    /**
     * instantiates and initializes a new builder generator.
     *
     * @param productUnit
     *            unit with the product class definition
     * @param productClassName
     *            name of the product class
     * @param builderUnit
     *            unit with the builder class
     */
    BuilderGenerator(CompilationUnit productUnit, String productClassName, CompilationUnit builderUnit) {
        this.builderUnit = builderUnit;
        this.productClassName = productClassName;
        this.productUnit = productUnit;

        createPackageDeclaration();
        createBuilderClass();
        copyImports();
    }

    private void createPackageDeclaration() {
        if (!builderUnit.getPackageDeclaration().isPresent()) {
            productUnit.getPackageDeclaration().ifPresent(builderUnit::setPackageDeclaration);
        }
    }

    private void copyImports() {
        productUnit.getImports().stream() //
                .filter(Util.not(this::isBuilderImport)) //
                .forEach(builderUnit::addImport);
    }

    private boolean isBuilderImport(ImportDeclaration impDec) {
        return impDec.getNameAsString().startsWith(BUILDER_PACKAGE);
    }

    private void createBuilderClass() {
        this.builderclass = builderUnit.findAll(ClassOrInterfaceDeclaration.class).stream() //
                .filter(c -> c.getNameAsString().equals(productClassName + CLASS_POSTFIX)) //
                .filter(Util.not(ClassOrInterfaceDeclaration::isInterface)) //
                .findFirst() //
                .orElseGet(() -> builderUnit.addClass(productClassName + CLASS_POSTFIX));
    }

    /**
     * Add a builder constructor for each constructor in the product class.
     */
    BuilderGenerator addConstructors() {
        addDefaultConstructorIfNeeded();
        productUnit.findAll(ConstructorDeclaration.class).stream() //
                .filter(ProductUtil::process) //
                .forEach(this::addMatchingConstructor);
        return this;
    }

    private void addDefaultConstructorIfNeeded() {
        if (!ProductUtil.hasExplicitConstructor(productUnit) && !ProductUtil.hasDefaultConstructor(builderUnit)) {
            ConstructorDeclaration builderconstructor = builderclass.addConstructor(Modifier.Keyword.PUBLIC);
            builderconstructor.createBody() //
                    .addStatement(FIELD + " = new " + productClassName + "();");
        }
    }

    private void addMatchingConstructor(ConstructorDeclaration productConstructor) {
        if (!hasMatchingConstructor(productConstructor)) {
            ConstructorDeclaration builderconstructor = builderclass.addConstructor(Modifier.Keyword.PUBLIC);
            productConstructor.getParameters().stream().forEach(builderconstructor::addParameter);
            builderconstructor.createBody() //
                    .addStatement(FIELD + " = " + invocation(productConstructor) + ";");
        }
    }

    private boolean hasMatchingConstructor(ConstructorDeclaration productConstructor) {
        return builderclass.findAll(ConstructorDeclaration.class).stream() //
                .filter(cd -> matchesParameter(cd, productConstructor)) //
                .findAny().isPresent();
    }

    /**
     * Adds a builder factory method for each product constructor.
     */
    BuilderGenerator addFactoryMethods() {
        addProductConstructor();
        if (!hasExplicitConstructor(productUnit)) {
            addDefaultFactoryMethod();
        }
        productUnit.findAll(ConstructorDeclaration.class).stream() //
                .filter(ProductUtil::process) //
                .forEach(this::addFactoryMethod);
        return this;
    }

    private void addDefaultFactoryMethod() {
        if (!hasDefaultFactoryMethod()) {
            MethodDeclaration factoryMethod = builderclass.addMethod(FACTORY_METHOD, Modifier.Keyword.PUBLIC,
                    Modifier.Keyword.STATIC);
            factoryMethod.setType(builderclass.getNameAsString());
            factoryMethod.createBody() //
                    .addStatement("return new " + builderclass.getNameAsString() + "(new " + productClassName + "());");
        }
    }

    private boolean hasDefaultFactoryMethod() {
        return builderclass.findAll(MethodDeclaration.class).stream() //
                .filter(MethodDeclaration::isStatic) //
                .filter(md -> md.getNameAsString().equals(FACTORY_METHOD)) //
                .filter(md -> md.getTypeAsString().equals(builderclass.getNameAsString())) //
                .filter(md -> md.getParameters().size() == 0) //
                .findAny().isPresent();
    }

    /**
     * Adds a product constructor to the builder.
     * <p>
     * signature {@code private Builder(Product product)}
     */
    void addProductConstructor() {
        if (!ProductUtil.hasProductConstructor(builderUnit, productClassName)) {
            ConstructorDeclaration constructor = builderclass.addConstructor(Modifier.Keyword.PRIVATE);
            constructor.addParameter(productClassName, FIELD);
            constructor.createBody() //
                    .addStatement(new AssignExpr(//
                            new FieldAccessExpr(new ThisExpr(), FIELD), //
                            new NameExpr(FIELD), //
                            Operator.ASSIGN));
        }
    }

    private void addFactoryMethod(ConstructorDeclaration productConstructor) {
        if (!hasMatchingFactoryMethod(productConstructor)) {
            MethodDeclaration factoryMethod = builderclass.addMethod(FACTORY_METHOD, Modifier.Keyword.PUBLIC,
                    Modifier.Keyword.STATIC);
            productConstructor.getParameters().stream().forEach(factoryMethod::addParameter);
            factoryMethod.setType(builderclass.getNameAsString());
            factoryMethod.createBody() //
                    .addStatement(//
                            "return new " + builderclass.getNameAsString() + "(" + invocation(productConstructor)
                                    + ");");
        }
    }

    private boolean hasMatchingFactoryMethod(ConstructorDeclaration productConstructor) {
        return builderclass.findAll(MethodDeclaration.class).stream() //
                .filter(MethodDeclaration::isStatic) //
                .filter(md -> md.getNameAsString().equals(FACTORY_METHOD)) //
                .filter(md -> md.getTypeAsString().equals(builderclass.getNameAsString())) //
                .filter(md -> matchesParameter(md, productConstructor)) //
                .findAny().isPresent();
    }

    private boolean matchesParameter(CallableDeclaration a, CallableDeclaration b) {
        if (a.getParameters().size() != b.getParameters().size()) {
            return false;
        }
        for (int n = 0; n < a.getParameters().size(); n++) {
            if (!a.getParameter(n).getTypeAsString().equals(b.getParameter(n).getTypeAsString())) {
                return false;
            }
        }
        return true;
    }

    private String invocation(ConstructorDeclaration cd) {
        return "new " + cd.getNameAsString() + "(" + (//
        cd.getParameters().stream().map(Parameter::getNameAsString).collect(Collectors.joining(","))) + ")";
    }

    /**
     * Adds a field for the product to the builder.
     */
    BuilderGenerator addProductField() {
        if (!builderclass.findAll(FieldDeclaration.class).stream() //
                .map(FieldDeclaration::getVariables) //
                .map(List::stream) //
                .flatMap(Function.identity()) //
                .filter(vd -> vd.getNameAsString().equals(FIELD)) //
                .findAny().isPresent()) {
            builderclass.addField(productClassName, FIELD, Modifier.Keyword.PRIVATE);
        }
        return this;
    }

    private boolean matchesParameter(MethodDeclaration md, String type) {
        return md.getParameters().size() == 1 && md.getParameter(0).getTypeAsString().equals(type);
    }

    /**
     * Adds a with method for each field in the product.
     */
    BuilderGenerator addWithMethods() {
        productUnit.findAll(FieldDeclaration.class).stream() //
                .filter(ProductUtil::process) //
                .map(FieldDeclaration::getVariables) //
                .flatMap(List::stream) //
                .forEach(this::addWithMethod);
        return this;
    }

    private void addWithMethod(VariableDeclarator vd) {
        addWithMethod(vd.getType().asString(), vd.getNameAsString());
    }

    private void addWithMethod(String type, String name) {
        if (!hasWithMethod(type, name)) {
            MethodDeclaration meth = builderclass.addMethod(makeWithName(name), Modifier.Keyword.PUBLIC);
            meth.addParameter(type, name);
            meth.setType(builderClassType());
            meth.createBody() //
                    .addStatement(new AssignExpr( //
                            new FieldAccessExpr(new NameExpr(FIELD), name), //
                            new NameExpr(name), //
                            AssignExpr.Operator.ASSIGN))
                    .addStatement(new ReturnStmt(new ThisExpr()));
        }
    }

    private boolean hasWithMethod(String type, String name) {
        return builderclass.findAll(MethodDeclaration.class).stream() //
                .filter(md -> md.getNameAsString().equals(makeWithName(name))) //
                .filter(md -> matchesParameter(md, type)) //
                .filter(md -> md.getType().equals(builderClassType())) //
                .findAny().isPresent();
    }

    /**
     * Adds the build method to the builder class.
     */
    BuilderGenerator addBuildMethod() {
        if (!hasBuildMethod()) {
            builderclass.addMethod(BUILD_METHOD, Modifier.Keyword.PUBLIC) //
                    .setType(productClassType()) //
                    .createBody() //
                    .addStatement(new ReturnStmt(new NameExpr(FIELD)));
        }
        return this;
    }

    private boolean hasBuildMethod() {
        return builderclass.findAll(MethodDeclaration.class).stream() //
                .filter(md -> md.getNameAsString().equals(BUILD_METHOD)) //
                .filter(md -> md.getType().equals(productClassType())) //
                .findAny().isPresent();
    }

    private ClassOrInterfaceType productClassType() {
        return new ClassOrInterfaceType(productClassName);
    }

    private String makeWithName(String name) {
        return WITH_PREFIX + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Returns the builder class.
     *
     * @return compilation unit with the builder class
     */
    CompilationUnit builderUnit() {
        return builderUnit;
    }

    /**
     * Adds an add method for each list implementing field in the product.
     */
    BuilderGenerator addAddMethods() {
        productUnit.findAll(FieldDeclaration.class).stream() //
                .filter(ProductUtil::process) //
                .map(FieldDeclaration::getVariables) //
                .flatMap(List::stream) //
                .filter(vd -> ProductUtil.implementsCollection(vd, productUnit)) //
                .forEach(this::addAddMethod);
        return this;
    }

    private void addAddMethod(VariableDeclarator vd) {
        Type itemType = vd.getType().asClassOrInterfaceType().getTypeArguments().get().get(0);
        addAddMethod(itemType, vd.getNameAsString());
    }

    private void addAddMethod(Type itemType, String fieldName) {
        if (!hasAddMethod(itemType, fieldName)) {
            MethodDeclaration meth = builderclass.addMethod(makeAddName(fieldName), Modifier.Keyword.PUBLIC);
            meth.addParameter(itemType, "item");
            meth.setType(builderClassType());
            meth.createBody() //
                    .addStatement(new MethodCallExpr(new FieldAccessExpr(new NameExpr(FIELD), fieldName), "add",
                            new NodeList<>(new NameExpr("item")))) //
                    .addStatement(new ReturnStmt(new ThisExpr()));
        }
    }

    /**
     * Checks for an add method in the builder class.
     * <p>
     * signature {@code Builder addName(Type item)}
     *
     * @param type
     *            Type of the list items
     * @param name
     *            name of the list field in the product class
     * @return {@code true} if the method exists
     */
    private boolean hasAddMethod(Type type, String name) {
        return builderclass.findAll(MethodDeclaration.class).stream() //
                .filter(md -> md.getNameAsString().equals(makeAddName(name))) //
                .filter(md -> matchesParameter(md, type.asString())) //
                .filter(md -> md.getType().equals(builderClassType())) //
                .findAny().isPresent();
    }

    private ClassOrInterfaceType builderClassType() {
        return new ClassOrInterfaceType(builderclass.getNameAsString());
    }

    private String makeAddName(String name) {
        return ADD_PREFIX + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

}
