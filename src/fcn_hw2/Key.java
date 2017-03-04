package fcn_hw2;
import java.util.Arrays;

public class Key<T> {

    private final T[] t;

    public Key(T[] t) {
        this.t = t;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Arrays.hashCode(this.t);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Key<T> other = (Key<T>) obj;
        if (!Arrays.equals(this.t, other.t)) {
            return false;
        }
        return true;
    }
}