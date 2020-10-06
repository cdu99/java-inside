package fr.umlv.javainside;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MethodHandleTests {
    @Test
    public void findStaticTest() throws NoSuchMethodException, IllegalAccessException {
        var lookup = MethodHandles.lookup();
        // Lookup correspond à la classe des tests
        var methodHandle = lookup.findStatic(Integer.class, "parseInt", MethodType.methodType(int.class, String.class));
        assertEquals(MethodType.methodType(int.class, String.class) ,methodHandle.type());
    }

    @Test
    public void findVirtualTest() throws NoSuchMethodException, IllegalAccessException {
        var lookup = MethodHandles.lookup();
        var methodHandle = lookup.findVirtual(String.class, "toUpperCase", MethodType.methodType(String.class));
        assertEquals(MethodType.methodType(String.class, String.class) ,methodHandle.type());
        // Expected value, value
    }

    @Test
    public void invokeExactStaticTest() throws Throwable {
        var lookup = MethodHandles.lookup();
        var methodHandle = lookup.findStatic(Integer.class, "parseInt", MethodType.methodType(int.class, String.class));
        var result = (int) methodHandle.invokeExact("555");
        assertEquals(555, result);
    }

    @Test
    public void invokeExactStaticWrongArgumentTest() throws Throwable {
        var lookup = MethodHandles.lookup();
        var methodHandle = lookup.findStatic(Integer.class, "parseInt", MethodType.methodType(int.class, String.class));
        Assertions.assertThrows(WrongMethodTypeException.class, () -> {
            methodHandle.invokeExact();
        });
    }

    @Test
    public void invokeExactVirutalTest() throws Throwable {
        var lookup = MethodHandles.lookup();
        var methodHandle = lookup.findVirtual(String.class, "toUpperCase", MethodType.methodType(String.class));
        var result = (String) methodHandle.invokeExact("bonjour");
        assertEquals("BONJOUR" ,result);
    }

    @Test
    public void invokeExactVirtualWrongArgumentTest() throws NoSuchMethodException, IllegalAccessException {
        var lookup = MethodHandles.lookup();
        var methodHandle = lookup.findVirtual(String.class, "toUpperCase", MethodType.methodType(String.class));
        Assertions.assertThrows(WrongMethodTypeException.class, () -> {
            methodHandle.invokeExact();
        });
    }

    @Test
    public void invokeStaticTest() throws Throwable {
        var lookup = MethodHandles.lookup();
        var methodHandle = lookup.findStatic(Integer.class, "parseInt", MethodType.methodType(int.class, String.class));
        Assertions.assertAll(
                () -> assertEquals(555, (Integer) methodHandle.invoke("555")),
                () -> Assertions.assertThrows(WrongMethodTypeException.class, () -> {
                    var s = (String) methodHandle.invokeExact("555");
                })
        );
    }

    @Test
    public void invokeVirtualTest() throws Throwable {
        var lookup = MethodHandles.lookup();
        var methodHandle = lookup.findVirtual(String.class, "toUpperCase", MethodType.methodType(String.class));
        var result = (String) methodHandle.invokeExact("bonjour");
        assertEquals("BONJOUR" ,result);
        Assertions.assertAll(
                () -> assertEquals("BONSOIR", (Object) methodHandle.invoke("bonsoir")),
                () -> Assertions.assertThrows(WrongMethodTypeException.class, () -> {
                    var s = (Double) methodHandle.invokeExact("BONJOUR");
                })
        );
    }

    @Test
    public void insertAndInvokeStaticTest() throws Throwable {
        var lookup = MethodHandles.lookup();
        var methodHandle = lookup.findStatic(Integer.class, "parseInt", MethodType.methodType(int.class, String.class));
        var methodHandleCopy = MethodHandles.insertArguments(methodHandle, 0, "123");
        Assertions.assertAll(
                () -> assertEquals(123, (Integer) methodHandleCopy.invoke()),
                () -> Assertions.assertThrows(WrongMethodTypeException.class, () -> {
                    var s = (String) methodHandleCopy.invokeExact();
                })
        );
    }
}
