package smisko11.gym_social.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException{
    public ConflictException(String message){
        super(HttpStatus.CONFLICT, message);
    }
}
