package undobutton.cloning;

import java.util.Map;

public interface ICustomCloner {
    Object clone(Object from, Cloner cloner, Map<Object, Object> clones);
}
