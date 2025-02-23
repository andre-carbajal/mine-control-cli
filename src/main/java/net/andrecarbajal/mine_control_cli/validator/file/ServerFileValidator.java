package net.andrecarbajal.mine_control_cli.validator.file;

import net.andrecarbajal.mine_control_cli.validator.core.IValidator;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ServerFileValidator implements IValidator {
    @Override
    public boolean isValid(String value) {
        return Files.exists(Path.of(value));
    }
}
