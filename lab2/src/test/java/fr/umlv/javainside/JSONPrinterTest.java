package fr.umlv.javainside;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPrinterTest {
    @Test
    public void JSONTest() {
        Person person = new Person("John", "Doe");
        Alien alien = new Alien(100, "Saturn");
        assertEquals("{\"firstName\": \"John\",\"lastName\": \"Doe\"}", JSONPrinter.toJSON(person));
    }
}
