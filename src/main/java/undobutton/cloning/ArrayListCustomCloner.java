package undobutton.cloning;

import java.util.ArrayList;
import java.util.Map;

public class ArrayListCustomCloner implements ICustomCloner {
    @Override
    public Object clone(Object from, Cloner cloner, Map<Object, Object> clones) {
        ArrayList<?> al = (ArrayList<?>) from;
        ArrayList<Object> result = new ArrayList<>(al.size());
        al.forEach(o -> result.add(cloner.internalClone(o)));
        return result;
    }
}
