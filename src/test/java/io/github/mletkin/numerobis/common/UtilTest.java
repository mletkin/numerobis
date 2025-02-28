/**
 * (c) 2025 by Ullrich Rieger
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
package io.github.mletkin.numerobis.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UtilTest {

    @Nested
    class MakeStream {

        @Test
        void nullMakesEmptyStream() {
            assertThat(Util.stream(null)) //
                    .isNotNull() //
                    .isEmpty();
        }

        @Test
        void listMakesStreamWithElements() {
            assertThat(Util.stream(List.of(1, 2, 3))).containsExactly(1, 2, 3);
        }
    }

    @Nested
    class CreateParentpath {

        @Test
        void pfadWirdErzeugt(@TempDir Path dir) {
            Util.createParentPath(dir.resolve("foo/bar/baz"));

            assertThat(dir.resolve("foo/bar")).exists();
            assertThat(dir.resolve("foo/bar/baz")).doesNotExist();

        }
    }
}
