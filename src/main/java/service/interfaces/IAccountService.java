package service.interfaces;

import domain.AccountDTO;
import org.springframework.http.ResponseEntity;
import response.BaseResponse;

import java.util.List;
import java.util.Map;

public interface IAccountService {
    BaseResponse signUp(AccountDTO account) throws Exception;

    Map<String, Object> checkKey(String key) throws Exception;

    Map<String, String> login(AccountDTO account) throws Exception;

    Map<String, String> refresh(String token) throws Exception;
}
