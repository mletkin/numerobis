public class TestClassIgnoreConstructor {
    int x;
    
    public TestClassIgnoreConstructor(int n) { }

    @Ignore
    public TestClassIgnoreConstructor(int n, int p) { }
}
