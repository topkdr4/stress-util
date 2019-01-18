/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class Argument<T> {
    private T val;


    public void set(T val) {
        this.val = val;
    }


    public T get(T def) {
        return val == null ? def : val;
    }


    public T get() {
        return val;
    }
}
