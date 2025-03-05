public class TestClass {

    int x;

    public static class Builder {

        private TestClass product;

        public Builder() {
            product = new TestClass();
        }

        public Builder withX(int x) {
            product.x = x;
            return this;
        }

        public TestClass build() {
            return product;
        }
    }
}
