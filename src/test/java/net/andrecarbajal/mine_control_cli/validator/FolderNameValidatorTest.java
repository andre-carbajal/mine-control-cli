package net.andrecarbajal.mine_control_cli.validator;

import net.andrecarbajal.mine_control_cli.validator.file.FolderNameValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FolderNameValidatorTest {
    private FolderNameValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FolderNameValidator();
    }

    @Test
    void validate_ValidName() {
        assertTrue(validator.isValid("validName"));
        assertTrue(validator.isValid("validName123"));
        assertTrue(validator.isValid("validName-123"));
        assertTrue(validator.isValid("validName_123"));
    }

    @Test
    void validate_EmptyValue() {
        assertFalse(validator.isValid(""));
    }

    @Test
    void validate_TooLong() {
        assertFalse(validator.isValid("a".repeat(256)));
    }

    @Test
    void validate_ReservedName() {
        assertFalse(validator.isValid("CON"));
        assertFalse(validator.isValid("COM1"));
        assertFalse(validator.isValid("LPT1"));
    }

    @Test
    void validate_InvalidChars() {
        assertFalse(validator.isValid("invalid\\chars"));
        assertFalse(validator.isValid("invalid:chars"));
        assertFalse(validator.isValid("invalid*chars"));
        assertFalse(validator.isValid("invalid?chars"));
        assertFalse(validator.isValid("invalid\"chars"));
        assertFalse(validator.isValid("invalid<chars"));
        assertFalse(validator.isValid("invalid>chars"));
        assertFalse(validator.isValid("invalid|chars"));
    }

    @Test
    void validate_LeadingOrTrailingSpaces() {
        assertFalse(validator.isValid(" leading"));
        assertFalse(validator.isValid("trailing "));
        assertFalse(validator.isValid(".leading"));
        assertFalse(validator.isValid("trailing."));
    }

    @Test
    void validate_ConsecutiveDots() {
        assertFalse(validator.isValid("consecutive..dots"));
    }

}