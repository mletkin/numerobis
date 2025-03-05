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

import java.util.Collections;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import io.github.mletkin.numerobis.plugin.Naming;

/**
 * Sort the members of each class in a compilation unit.
 * <p>
 * Intended for the sorting of builder classes.
 * <ol>
 * <li>static fields
 * <li>non-static fields
 * <li>constructors
 * <li>with methods
 * <li>build method
 * <li>other methods
 * <li>anything else
 * </ol>
 * Does not work with
 * <ul>
 * <li>static and non-static code blocks
 * <li>line comments on class level
 * </ul>
 */
public class Sorter {

    private Naming naming;

    public Sorter(Naming naming) {
        this.naming = naming;
    }

    /**
     * Sort the members in each class in a compilation unit.
     * <p>
     * The output object it the input object.
     *
     * @param  cu compilation unit to process
     * @return    the processed compilation unit
     */
    public CompilationUnit sort(CompilationUnit cu) {
        cu.getTypes().stream() //
                .map(TypeDeclaration::getMembers) //
                .forEach(this::sort);
        return cu;
    }

    /**
     * Sort the members of a {@code NodeList}.
     *
     * @param member {@code NodeList} object to sort
     */
    private void sort(NodeList<BodyDeclaration<?>> member) {
        Collections.sort(member, this::compare);
    }

    private int compare(BodyDeclaration<?> bd1, BodyDeclaration<?> bd2) {
        return Integer.compare(value(bd1), value(bd2));
    }

    /**
     * Assign an order index to a {@code BodyDeclaration} object.
     *
     * @param  declaration object to assess
     * @return             index value
     */
    private int value(BodyDeclaration<?> declaration) {
        if (declaration instanceof FieldDeclaration fd) {
            return fd.isStatic() ? 10 : 11;
        }

        if (declaration instanceof ConstructorDeclaration) {
            return 20;
        }

        if (declaration instanceof MethodDeclaration md) {
            if (md.isStatic() && md.getNameAsString().startsWith(naming.factoryMethod())) {
                return 30;
            }
            if (md.getNameAsString().startsWith(naming.mutatorPrefix())) {
                return 31;
            }
            if (md.getNameAsString().equals(naming.buildMethod())) {
                return 32;
            }
            return 33;
        }
        return 40;
    }

}
