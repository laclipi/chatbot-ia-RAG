
package com.laura.chatbot_ia.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Centraliza el tratamiento de errores de los controladores REST.
@RestControllerAdvice
public class GlobalExceptionHandler {

        // Gestiona los errores de validación de los objetos recibidos en el body.
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidationErrors(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {

                // Recuperamos el primer mensaje de validación que haya fallado.
                String message = exception.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .findFirst()
                                .map(error -> error.getDefaultMessage())
                                .orElse("Los datos enviados no son válidos");

                ApiError apiError = new ApiError(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                message,
                                request.getRequestURI());

                return ResponseEntity.badRequest().body(apiError);
        }

        // Devuelve 404 cuando el negocio solicitado no existe.
        @ExceptionHandler(BusinessNotFoundException.class)
        public ResponseEntity<ApiError> handleBusinessNotFound(
                        BusinessNotFoundException exception,
                        HttpServletRequest request) {

                ApiError apiError = new ApiError(
                                HttpStatus.NOT_FOUND.value(),
                                HttpStatus.NOT_FOUND.getReasonPhrase(),
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
        }

        @ExceptionHandler(OfferedServiceNotFoundException.class)
        public ResponseEntity<ApiError> handleOfferedServiceNotFound(
                        OfferedServiceNotFoundException exception,
                        HttpServletRequest request) {

                ApiError apiError = new ApiError(
                                HttpStatus.NOT_FOUND.value(),
                                HttpStatus.NOT_FOUND.getReasonPhrase(),
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
        }

        // Devuelve HTTP 409 cuando una reserva entra en conflicto con otra existente.
        @ExceptionHandler(AppointmentConflictException.class)
        public ResponseEntity<ApiError> handleAppointmentConflict(
                        AppointmentConflictException exception,
                        HttpServletRequest request) {

                // Construimos el error siguiendo el formato que ya utiliza la API.
                ApiError apiError = new ApiError(
                                HttpStatus.CONFLICT.value(),
                                HttpStatus.CONFLICT.getReasonPhrase(),
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
        }

        // Devuelve HTTP 409 cuando el email ya está registrado.
        // ResponseEntity escribe el estado y el cuerpo en esta misma petición.
        // ResponseStatusException llamaría a sendError, el contenedor reenviaría
        // a /error y Spring Security convertiría ese reenvío en 403.
        @ExceptionHandler(EmailAlreadyRegisteredException.class)
        public ResponseEntity<ApiError> handleEmailAlreadyRegistered(
                        EmailAlreadyRegisteredException exception,
                        HttpServletRequest request) {

                ApiError apiError = new ApiError(
                                HttpStatus.CONFLICT.value(),
                                HttpStatus.CONFLICT.getReasonPhrase(),
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
        }

        // Devuelve 401 cuando el email del token no tiene una cuenta.
        // ResponseEntity evita que Spring Security convierta el error en 403.
        @ExceptionHandler(AuthenticatedUserNotFoundException.class)
        public ResponseEntity<ApiError> handleAuthenticatedUserNotFound(
                        AuthenticatedUserNotFoundException exception,
                        HttpServletRequest request) {

                ApiError apiError = new ApiError(
                                HttpStatus.UNAUTHORIZED.value(),
                                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
        }

}
