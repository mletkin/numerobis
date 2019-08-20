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
package io.github.mletkin.numerobis;

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;

import io.github.mletkin.numerobis.annotation.Ignore;

/**
 * Convenience Methods for the Product class.
 */
public final class ProductUtil {

    private ProductUtil() {
        // prevent instantiation
    }

    /**
     * Checks a compilation unit for a given class.
     *
     * @param cu
     *            compilation unit to check
     * @param className
     *            name of the searched class
     * @return {@code true} if the unit contains a class with the given name
     */
    static boolean containsClass(CompilationUnit cu, String className) {
        return cu.findAll(ClassOrInterfaceDeclaration.class) //
                .stream().anyMatch(c -> c.getNameAsString().equals(className));
    }

    /**
     * Checks a compilation unit for a constructor.
     *
     * @param cu
     *            compilation unit to check
     * @return {@code true} if the compilation unit contains a constructor
     */
    static boolean hasExplicitConstructor(CompilationUnit cu) {
        return !cu.findAll(ConstructorDeclaration.class).isEmpty();
    }

    /**
     * Checks a compilation unit for a default constructor.
     *
     * @param cu
     *            compilation unit to check
     * @return {@code true} if the compilation unit contains a default constructor.
     */
    static boolean hasDefaultConstructor(CompilationUnit cu) {
        return cu.findAll(ConstructorDeclaration.class).stream() //
                .anyMatch(cd -> cd.getParameters().isEmpty());
    }

    /**
     * Checks a compilation unit for a non-private constructor.
     *
     * @param cu
     *            compilation unit to check
     * @return {@code true} if the compilation unit contains a non-private
     *         constructor.
     */
    static boolean hasUsableConstructor(CompilationUnit cu) {
        List<ConstructorDeclaration> constructorList = cu.findAll(ConstructorDeclaration.class);
        return constructorList.isEmpty() || constructorList.stream().anyMatch(ProductUtil::process);
    }

    /**
     * Checks a field declaration for builder usage.
     *
     * @param fd
     *            field declartion to check
     * @return {@code true} if the field should have a with-method
     */
    static boolean process(FieldDeclaration fd) {
        return !fd.isPrivate() && !fd.isAnnotationPresent(Ignore.class);
    }

    /**
     * Checks a constructor declaration for builder usage.
     *
     * @param cd
     *            constructor declartion to check
     * @return {@code true} if the builder should have a corresponding constructor
     */
    static boolean process(ConstructorDeclaration cd) {
        return !cd.isPrivate() && !cd.isAnnotationPresent(Ignore.class);
    }
}
