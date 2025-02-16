package net.andrecarbajal.mine_control_cli.validator;

import net.andrecarbajal.mine_control_cli.validator.property.RamPropertyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RamPropertyValidatorTest {

    private RamPropertyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RamPropertyValidator();
    }

    @Test
    void validate_ValidGigabyteFormat_NoException() {
        assertDoesNotThrow(() -> validator.validate("2G"));
        assertDoesNotThrow(() -> validator.validate("4G"));
    }

    @Test
    void validate_ValidMegabyteFormat_NoException() {
        assertDoesNotThrow(() -> validator.validate("2048MB"));
        assertDoesNotThrow(() -> validator.validate("4096MB"));
    }

    @Test
    void validate_DecimalValues_NoException() {
        assertDoesNotThrow(() -> validator.validate("1.5G"));
        assertDoesNotThrow(() -> validator.validate("1024.5MB"));
    }

    @Test
    void validate_EmptyValue_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("")
        );
        assertEquals("Value cannot be empty", exception.getMessage());
    }

    @Test
    void validate_InvalidFormat_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("2KB")
        );
        assertTrue(exception.getMessage().contains("must be a number followed by G or MB"));
    }

    @Test
    void validate_ZeroValue_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("0G")
        );
        assertTrue(exception.getMessage().contains("must be greater than 0"));
    }
}