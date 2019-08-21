public class EmptyWithIgnoredConstructor {
    
    public EmptyWithIgnoredConstructor(int n) { }

    @Ignore
    public EmptyWithIgnoredConstructor(int n, int p) { }
}
