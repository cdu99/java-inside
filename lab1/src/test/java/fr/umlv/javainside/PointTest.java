package fr.umlv.javainside;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PointTest {
    @Test
    public void coordonateTest() {
        var point = new Point(5, 3);
        assertEquals(5, point.x());
        assertEquals(3, point.y());
    }
}