public class TestClassWithoutConstructor {
    int x;
    
    private TestClassWithoutConstructor(int n) { }

    @Ignore
    public TestClassWithoutConstructor(int n, int p) { }
}
