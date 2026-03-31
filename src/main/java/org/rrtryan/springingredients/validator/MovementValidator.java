package org.rrtryan.springingredients.validator;

import org.rrtryan.springingredients.entity.Movement;
import org.rrtryan.springingredients.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class MovementValidator {
    public MovementValidator() {}

    public void validate(Movement movement) throws BadRequestException {
        if (movement == null) {
            throw new BadRequestException("Movement cannot be null");
        }
        if (movement.getType() == null) {
            throw new BadRequestException("Movement type cannot be null");
        }
        if (movement.getQuantity() == null) {
            throw new BadRequestException("Movement quantity cannot be null");
        }
        if (movement.getQuantity() < 0) {
            throw new BadRequestException("Movement quantity cannot be negative");
        }
    }
}
