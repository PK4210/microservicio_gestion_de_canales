package org.fiuni.mytube_channels.exception;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Manejar excepciones generales
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse errorDetails = new ErrorResponse("Ocurrio un error en el servidor", request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Manejar ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse errorDetails = new ErrorResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Entrada inválida", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Conflicto", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DatabaseOperationException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseOperationException(DatabaseOperationException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Error en la base de datos", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleOperationNotAllowedException(OperationNotAllowedException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Operación no permitida", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Acceso no autorizado", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(JDBCConnectionException.class)
    public ResponseEntity<String> handleJDBCConnectionException(JDBCConnectionException ex) {
        // Manejo específico para errores de conexión
        String message = "Error de conexión a la base de datos: " + ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
        // Manejo de otras excepciones de acceso a datos
        String message = "Error de acceso a datos (posiblemente murio la BD): " + ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

}