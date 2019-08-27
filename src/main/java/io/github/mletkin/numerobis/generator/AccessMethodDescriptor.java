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

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.Type;

/**
 * Describes an access method for a class.
 */
class AccessMethodDescriptor {
    String methodName;
    String fieldName;
    Type returnType;

    static class Generator {
        FieldDeclaration field;

        Generator(FieldDeclaration field) {
            this.field = field;
        }

        /**
         * Produces a stream of method descriptors from a field declaration.
         *
         * @return Stream<AccessMethodDescriptor>
         */
        Stream<AccessMethodDescriptor> stream() {
            return field.getVariables().stream() //
                    .map(this::map);
        }

        private AccessMethodDescriptor map(VariableDeclarator vd) {
            AccessMethodDescriptor result = new AccessMethodDescriptor();
            result.methodName = methodName(vd);
            result.fieldName = vd.getNameAsString();
            result.returnType = vd.getType();
            return result;
        }

        private String methodName(VariableDeclarator vd) {
            return vd.getNameAsString();
        }
    }
}
