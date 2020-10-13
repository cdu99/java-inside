package fr.umlv.javainside;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;
import java.util.function.Consumer;

public interface Logger {
    public void log(String message);

    public static Logger of(Class<?> declaringClass, Consumer<? super String> consumer) {
        Objects.requireNonNull(declaringClass);
        Objects.requireNonNull(consumer);

        var mh = createLoggingMethodHandle(declaringClass, consumer);
        return new Logger() {
            @Override
            public void log(String message) {
                try {
                    mh.invokeExact(message);
                } catch(RuntimeException | Error e) {
                    throw e;
                } catch(Throwable t) {
                    throw new UndeclaredThrowableException(t);
                }
            }
        };
    }

    class Impl {
        // Pas de champs privé dans une interface donc on fait une classe
        private static final MethodHandle ACCEPT;
        static {
            var lookup = MethodHandles.lookup();
            try {
                ACCEPT = lookup.findVirtual(Consumer.class, "accept", MethodType.methodType(void.class, Object.class));
            }
            catch (NoSuchMethodException | IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
    }

    private static MethodHandle createLoggingMethodHandle(Class<?> declaringClass, Consumer<? super String> consumer){
        var methodHandle = Impl.ACCEPT.bindTo(consumer);
        methodHandle = methodHandle.asType(MethodType.methodType(void.class, String.class));
        return methodHandle;
    }
}