package net.andrecarbajal.mine_control_cli.validator;

import net.andrecarbajal.mine_control_cli.validator.property.JavaPathPropertyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaPathPropertyValidatorTest {

    private JavaPathPropertyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new JavaPathPropertyValidator();
    }

    @Test
    void validate_ValidWindowsPaths_NoException() {
        assertDoesNotThrow(() -> validator.validate("D:/Java/bin/java.exe"));
        assertDoesNotThrow(() -> validator.validate("C:/Program Files/Java/bin/javaw.exe"));
        assertDoesNotThrow(() -> validator.validate("C:\\Users\\andre\\.jabba\\jdk\\17-zulu\\bin\\java.exe"));
    }

    @Test
    void validate_ValidUnixPaths_NoException() {
        assertDoesNotThrow(() -> validator.validate("/opt/java/bin/java"));
        assertDoesNotThrow(() -> validator.validate("/usr/local/bin/javaw"));
    }

    @Test
    void validate_EmptyPath_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("")
        );
        assertEquals("Value cannot be empty", exception.getMessage());
    }

    @Test
    void validate_InvalidPathFormat_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("invalid/path")
        );
        assertEquals("Invalid path format", exception.getMessage());
    }

    @Test
    void validate_PathWithInvalidCharacters_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("C:/Program Files/Java/bin|/java.exe")
        );
        assertTrue(exception.getMessage().startsWith("Invalid character in path:"));
    }

    @Test
    void validate_PathWithInvalidComponentNames_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("C:/Program Files/Java/bin/ space/java.exe")
        );
        assertTrue(exception.getMessage().startsWith("Invalid component name:"));
    }

    @Test
    void validate_NonJavaExecutable_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("C:/Program Files/notepad.exe")
        );
        assertEquals("Path does not end with 'java', 'java.exe', 'javaw', or 'javaw.exe'", exception.getMessage());
    }
}
