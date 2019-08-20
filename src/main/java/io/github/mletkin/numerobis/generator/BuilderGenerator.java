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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import io.github.mletkin.numerobis.common.Util;

/**
 * Generates builder classes product classes in a seperate compilation unit.
 */
public class BuilderGenerator {

    private static final String BUILDER_PACKAGE = "io.github.mletkin.numerobis";
    private final static String FIELD = "product";
    private final static String CLASS_POSTFIX = "Builder";

    final static String BUILD_METHOD = "build";
    final static String WITH_PREFIX = "with";

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
     * Add a constructor for each constructor in the product class.
     */
    BuilderGenerator addConstructors() {
        addDefaultConstructorIfNeeded();
        productUnit.findAll(ConstructorDeclaration.class).stream() //
                .filter(ProductUtil::process) //
                .forEach(this::addConstructor);
        return this;
    }

    private void addDefaultConstructorIfNeeded() {
        if (!ProductUtil.hasExplicitConstructor(productUnit) && !ProductUtil.hasDefaultConstructor(builderUnit)) {
            ConstructorDeclaration builderconstructor = builderclass.addConstructor(Modifier.Keyword.PUBLIC);
            builderconstructor.createBody() //
                    .addStatement(FIELD + " = new " + productClassName + "();");
        }
    }

    private void addConstructor(ConstructorDeclaration productConstructor) {
        if (!hasConstructor(productConstructor)) {
            ConstructorDeclaration builderconstructor = builderclass.addConstructor(Modifier.Keyword.PUBLIC);
            productConstructor.getParameters().stream().forEach(builderconstructor::addParameter);
            builderconstructor.createBody() //
                    .addStatement(FIELD + " = " + invocation(productConstructor) + ";");
        }
    }

    private boolean hasConstructor(ConstructorDeclaration productConstructor) {
        return builderclass.findAll(ConstructorDeclaration.class).stream() //
                .filter(cd -> matchesParameter(cd, productConstructor)) //
                .findAny().isPresent();
    }

    private boolean matchesParameter(ConstructorDeclaration a, ConstructorDeclaration b) {
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
            meth.setType(new ClassOrInterfaceType(builderclass.getNameAsString()));
            meth.createBody() //
                    .addStatement(FIELD + "." + name + " = " + name + ";") //
                    .addStatement(new ReturnStmt(new ThisExpr()));
        }
    }

    private boolean hasWithMethod(String type, String name) {
        return builderclass.findAll(MethodDeclaration.class).stream() //
                .filter(md -> md.getNameAsString().equals(makeWithName(name))) //
                .filter(md -> matchesParameter(md, type)) //
                .filter(md -> md.getType().equals(new ClassOrInterfaceType(builderclass.getNameAsString()))) //
                .findAny().isPresent();
    }

    /**
     * Adds the build method to the builder class.
     */
    BuilderGenerator addBuildMethod() {
        if (!hasBuildMethod()) {
            builderclass.addMethod(BUILD_METHOD, Modifier.Keyword.PUBLIC) //
                    .setType(new ClassOrInterfaceType(productClassName)) //
                    .createBody() //
                    .addStatement("return " + FIELD + ";");
        }
        return this;
    }

    private boolean hasBuildMethod() {
        return builderclass.findAll(MethodDeclaration.class).stream() //
                .filter(md -> md.getNameAsString().equals(BUILD_METHOD)) //
                .filter(md -> md.getType().equals(new ClassOrInterfaceType(productClassName))) //
                .findAny().isPresent();
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
}
