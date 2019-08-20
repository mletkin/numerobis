/**
 * File comment.
 */
package foo.bar.baz;

import di.da.dum;

/**
 * class
 */
public class SortTest {

    public static class Foo {

    }

    /**
     * Method comment
     * 
     * @param x
     * @return
     */
    public Object foo(int x) {
        return null;
    }

    private TestClass product = new TestClass();

    
    public TestClass build() {
        return product;
    }

    public TestClassBuilder withX(int x) {
        // sue me
        product.x = x;
        return this;
    }

    /* Foo bekommt */
    /* Wert 10 */
    int foo = 10;
    static int bla = 10;

    /**
     * constructor comment.
     */
    SortTest() {
        // nothing to do
    }
}
