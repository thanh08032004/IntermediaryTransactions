package hsf302.group3.intermediarytransactions.exception;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied() {
        return "redirect:/403";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound() {
        return "redirect:/404";
    }

}