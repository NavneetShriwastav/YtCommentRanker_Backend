package com.navneet.YTCommentRanker.exception;

import com.navneet.YTCommentRanker.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleVideoNotFound(VideoNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponseDTO(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex) {
        ex.printStackTrace();
        return new ResponseEntity<>(new ErrorResponseDTO("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
