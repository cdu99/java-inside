package fr.umlv.javainside;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class JSONPrinter {
//    public static String toJSON(Person person) {
//        return """
//      {
//        "firstName": "%s",
//        "lastName": "%s"
//      }
//      """.formatted(person.firstName(), person.lastName());
//    }
//
//    public static String toJSON(Alien alien) {
//        return """
//      {
//        "age": %s,
//        "planet": "%s"
//      }
//      """.formatted(alien.age(), alien.planet());
//    }

    public static String toJSON(Record record) {
        var components = record.getClass().getRecordComponents();
        return "{" +Arrays.stream(components)
                .map(x -> "\"" + x.getName() + "\": " + invokeAccessor(x.getAccessor(), record))
                .collect(Collectors.joining(",")) + "}";
    }

    private static Object invokeAccessor(Method accessor, Record record) {
        try {
            var isItString = 0;
            var value = accessor.invoke(record);
            if (value instanceof String) {
                isItString++;
            }
            if (isItString == 0) return accessor.invoke(record);
            else return "\"" + accessor.invoke(record) + "\"";

        } catch (IllegalAccessException e) {
            throw (IllegalAccessError) new IllegalAccessError().initCause(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            if (cause instanceof  Error error) {
                throw error;
            }
            throw new UndeclaredThrowableException(cause);
        }
    }

    public static void main(String[] args) {
        var person = new Person("John", "Doe");
        System.out.println(toJSON(person));
        var alien = new Alien(100, "Saturn");
        System.out.println(toJSON(alien));
        var book = new Book("book-title", 1999);
        System.out.println(toJSON(book));
    }
}
