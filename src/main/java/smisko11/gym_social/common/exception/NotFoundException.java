package smisko11.gym_social.common.exception;


import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException{
    public NotFoundException(String resource, Long id){
        super(HttpStatus.NOT_FOUND, resource + " sa id: " + id + " ne postoji");
    }
}
