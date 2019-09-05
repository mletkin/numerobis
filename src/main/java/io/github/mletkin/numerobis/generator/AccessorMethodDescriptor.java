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

import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.Type;

import io.github.mletkin.numerobis.common.Util;
import io.github.mletkin.numerobis.generator.common.ClassUtil;

/**
 * Describes an accessor method for a class.
 */
class AccessorMethodDescriptor {
    String methodName;
    String fieldName;
    Type fieldType;
    boolean streamAccessor;

    static class Generator {
        FieldDeclaration field;
        private CompilationUnit cu;
        private String prefix;

        Generator(FieldDeclaration field, String prefix, CompilationUnit cu) {
            this.field = field;
            this.cu = cu;
            this.prefix = prefix;
        }

        /**
         * Produces a stream of method descriptors from a field declaration.
         *
         * @return Stream<AccessorMethodDescriptor>
         */
        Stream<AccessorMethodDescriptor> stream() {
            return field.getVariables().stream() //
                    .map(this::map);
        }

        private AccessorMethodDescriptor map(VariableDeclarator vd) {
            AccessorMethodDescriptor result = new AccessorMethodDescriptor();
            result.methodName = methodName(vd);
            result.fieldName = vd.getNameAsString();
            result.fieldType = vd.getType();
            result.streamAccessor = ClassUtil.isCollection(vd, cu);
            return result;
        }

        private String methodName(VariableDeclarator vd) {
            return prefix != null && !prefix.equals("") //
                    ? prefix + Util.firstLetterUppercase(vd.getNameAsString())
                    : vd.getNameAsString();
        }
    }
}
