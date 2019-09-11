import java.util.List;
public class WithListWithVarargMutator {
    List<String> x = new ArrayList<>();
    class Builder {
        Builder withX(String... foo) {
            
        }
    }
}
