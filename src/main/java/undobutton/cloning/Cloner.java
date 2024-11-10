package undobutton.cloning;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

public class Cloner {
    private final static Objenesis objenesis = new ObjenesisStd();
    // Cloners with arbitrary class matching
    private final List<CustomClonerWithCondition> customCloners = new java.util.ArrayList<>();
    // Map a class to its cloner
    private final Map<Class<?>, ICustomCloner> clonerByClass = new HashMap<>();
    private final Set<Class<?>> ignoredClasses = new HashSet<>();
    // Logging
    private boolean logCloning = false;
    // Fields cache
    private final Map<Class<?>, List<Field>> fieldsCache = new HashMap<>();
    // Clones
    private Map<Object, Object> clones = null;

    public Cloner() {
        initialise();
    }

    public void setLogCloning(boolean logCloning) {
        this.logCloning = logCloning;
    }

    public boolean getLogCloning() {
        return logCloning;
    }

    protected void initialise() {
        Arrays.asList(String.class, Integer.class, Long.class, Boolean.class, Class.class, Float.class, Double.class, Character.class, Byte.class, Short.class, Void.class, URI.class, URL.class, UUID.class).forEach(this::addIgnoredClass);
        addCustomCloner(ArrayList.class, new ArrayListCustomCloner());
    }

    public void addIgnoredClass(Class<?> clazz) {
        ignoredClasses.add(clazz);
    }

    public void addCustomCloner(Predicate<Class<?>> condition, ICustomCloner cloner) {
        customCloners.add(new CustomClonerWithCondition(condition, cloner));
    }

    public void addCustomCloner(Class<?> clazz, ICustomCloner cloner) {
        clonerByClass.put(clazz, cloner);
    }

    public void logCloningClass(Class<?> clazz) {
        if (logCloning) {
            System.out.println("cloning> " + clazz.getName());
        }
    }

    public void logCloningField(Field field) {
        if (logCloning) {
            System.out.println("cloned field> " + field.getDeclaringClass() + " > " + field.getName() + " of type " + field.getType().getName());
        }
    }

    public void initCloningBatch() {
        clones = new IdentityHashMap<>();
    }

    public void endCloningBatch() {
        clones = null;
    }

    @SuppressWarnings("unchecked")
    public <T> T clone(T object) {
        return (T) internalClone(object);
    }

    public void cloneClassFieldsTo(Class<?> clazz, Object from, Object to) {
        List<Field> fields = getFields(clazz);
        try {
            for (Field field : fields) {
                field.set(to, internalClone(field.get(from)));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void moveClassFieldsTo(Class<?> clazz, Object from, Object to) {
        List<Field> fields = getFields(clazz);
        try {
            for (Field field : fields) {
                field.set(to, field.get(from));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected Object internalClone(Object from) {
        if (from == null) {
            return null;
        }
        Class<?> clazz = from.getClass();
        if (shouldIgnoreClass(clazz)) {
            return from;
        }
        Object cached = clones.get(from);
        if (cached != null) {
            return cached;
        }
        ICustomCloner cloner = getClonerForClass(clazz);
        return cloner.clone(from, this, clones);
    }

    protected boolean shouldIgnoreClass(Class<?> clazz) {
        if (Enum.class.isAssignableFrom(clazz)) {
            return true;
        }
        return ignoredClasses.contains(clazz);
    }

    protected ICustomCloner getClonerForClass(Class<?> clazz) {
        ICustomCloner cloner = clonerByClass.get(clazz);
        if (cloner == null) {
            for (CustomClonerWithCondition customCloner : customCloners) {
                if (customCloner.condition.test(clazz)) {
                    cloner = customCloner.cloner;
                    break;
                }
            }
            if (cloner == null) {
                if (clazz.isArray()) {
                    cloner = new ArrayCloner(clazz);
                } else {
                    cloner = new DefaultCloner(clazz);
                }
            }
            clonerByClass.put(clazz, cloner);
        }
        return cloner;
    }

    protected List<Field> getFields(Class<?> clazz) {
        List<Field> fields = fieldsCache.get(clazz);
        if (fields == null) {
            fields = new java.util.ArrayList<>();
            for (Field field : clazz.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (!field.isSynthetic() && !Modifier.isStatic(modifiers)) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    fields.add(field);
                }
            }
            fieldsCache.put(clazz, fields);
        }
        return fields;
    }

    private static class CustomClonerWithCondition {
        final Predicate<Class<?>> condition;
        final ICustomCloner cloner;

        CustomClonerWithCondition(Predicate<Class<?>> condition, ICustomCloner cloner) {
            this.condition = condition;
            this.cloner = cloner;
        }
    }

    protected class DefaultCloner implements ICustomCloner {
        final List<Field> fields;
        final ObjectInstantiator<?> instantiator;

        public DefaultCloner(Class<?> clazz) {
            instantiator = objenesis.getInstantiatorOf(clazz);
            List<Field> inheritedFields = new ArrayList<>();
            do {
                inheritedFields.addAll(getFields(clazz));
                clazz = clazz.getSuperclass();
            } while (clazz != null && clazz != Object.class);
            fields = inheritedFields;
        }

        @Override
        public Object clone(Object from, Cloner cloner, Map<Object, Object> clones) {
            logCloningClass(from.getClass());
            Object newInstance = instantiator.newInstance();
            clones.put(from, newInstance);
            try {
                for (Field field : fields) {
                    Object fieldObject = field.get(from);
                    Object fieldObjectClone = cloner.internalClone(fieldObject);
                    field.set(newInstance, fieldObjectClone);
                    if (fieldObject != fieldObjectClone) {
                        cloner.logCloningField(field);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return newInstance;
        }
    }

    protected class ArrayCloner implements ICustomCloner {
        final Class<?> componentType;
        final boolean immutableOrPrimitive;

        public ArrayCloner(Class<?> clazz) {
            this.componentType = clazz.getComponentType();
            this.immutableOrPrimitive = componentType.isPrimitive() || ignoredClasses.contains(componentType);
        }

        @Override
        public Object clone(Object from, Cloner cloner, Map<Object, Object> clones) {
            logCloningClass(from.getClass());
            int length = Array.getLength(from);
            Object newInstance = Array.newInstance(componentType, length);
            clones.put(from, newInstance);
            if (immutableOrPrimitive) {
                System.arraycopy(from, 0, newInstance, 0, length);
            } else {
                for (int i = 0; i < length; i++) {
                    Array.set(newInstance, i, cloner.internalClone(Array.get(from, i)));
                }
            }
            return newInstance;
        }
    }

}
