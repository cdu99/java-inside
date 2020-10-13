package fr.umlv.javainside;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoggerTest {
    @Test
    public void of() {
        class Foo {}
        var logger = Logger.of(Foo.class, __ -> {});
        assertNotNull(logger);
    }

    @Test
    public void ofError() {
        class Foo {}
        Assertions.assertAll(
                () -> assertThrows(NullPointerException.class, () -> Logger.of(null, __ -> {})),
                () -> assertThrows(NullPointerException.class, () -> Logger.of(Foo.class, null))
        );
    }

    @Test
    public void log() {
        class Foo {}
        var logger = Logger.of(Foo.class, message -> {
            assertEquals("Hello", message);
        });
        logger.log("Hello");
    }

    @Test
    public void logWithNullValue() {
        class Foo {}
        var logger = Logger.of(Foo.class, message -> {
            assertEquals(null, message); // Assertions::assertNull
        });
        logger.log(null);
    }
}
