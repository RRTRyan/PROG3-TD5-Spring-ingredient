package org.rrtryan.springingredients.controller;

import org.rrtryan.springingredients.entity.Ingredient;
import org.rrtryan.springingredients.entity.Movement;
import org.rrtryan.springingredients.entity.StockMovement;
import org.rrtryan.springingredients.entity.enums.UnitTypeEnum;
import org.rrtryan.springingredients.exception.BadRequestException;
import org.rrtryan.springingredients.exception.NotFoundException;
import org.rrtryan.springingredients.service.IngredientService;
import org.rrtryan.springingredients.validator.MovementValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/ingredients")
public class IngredientController {
    private final IngredientService ingredientService;
    private final MovementValidator  movementValidator;
    public IngredientController(IngredientService ingredientService, MovementValidator movementValidator) {
        this.ingredientService = ingredientService;
        this.movementValidator = movementValidator;
    }

    @GetMapping
    public ResponseEntity<?> getAllIngredients() {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ingredientService.findIngredients());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findIngredientById(@PathVariable(value = "id") Integer id) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ingredientService.findIngredientById(id));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<?> findIngredientStockById(@PathVariable(value = "id") Integer id,
                                                     @RequestParam(value = "at", required = false) Instant t,
                                                     @RequestParam(value = "unit", required = false) UnitTypeEnum unit) {
        try {
            if (t == null || unit == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Either mandatory query parameter `at` or `unit` is not provided.");
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ingredientService.getStockValueAt(t, id));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping("/{id}/stockMovements")
    public ResponseEntity<?> getStockMovementBetween(@PathVariable(value = "id") Integer id,
                                                 @RequestParam(value = "from") Instant from,
                                                 @RequestParam(value = "to") Instant to) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ingredientService.findIngredientById(id).getStockMovementBetween(from, to));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/stockMovements")
    public ResponseEntity<?> addStockMovement(@PathVariable(value = "id") Integer id,
                                              @RequestBody List<Movement> movements) {
        try {
            if (id == null) {
                throw new BadRequestException("Missing id");
            }
            if (movements == null) {
                throw new BadRequestException("Missing movements");
            }
            for (Movement movement : movements) {
                movementValidator.validate(movement);
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ingredientService.addStockMovement(id, movements));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
