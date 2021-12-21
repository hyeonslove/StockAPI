package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.AccountDTO;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import repository.AccountMapper;
import response.BaseResponse;
import service.interfaces.IAccountService;
import util.Jwt;

import javax.annotation.Resource;
import java.util.*;

@Service
public class AccountService implements IAccountService {
    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private Jwt jwt;

    @Override
    public BaseResponse signUp(AccountDTO account) throws Exception {
        // 이메일 중복체크
        if (accountMapper.isAccountToEmail(account.getEmail()) != 0)
            return new BaseResponse("이미 존재하는 이메일입니다.", HttpStatus.OK);

        // 닉네임 중복체크
        if (accountMapper.isAccountToNickName(account.getNickname()) != 0)
            return new BaseResponse("이미 존재하는 닉네임입니다.", HttpStatus.OK);

        // 비밀번호 암호화
        account.setPassword(BCrypt.hashpw(account.getPassword(), BCrypt.gensalt()));
        try {
            accountMapper.signUp(account);  // 회원 가입
        } catch (Exception ex) {
            return new BaseResponse("알 수 없는 원인으로 회원가입에 실패하였습니다.", HttpStatus.OK);
        }

        // salt를 설정해주기위해 uid를 가져옴
        Long uid = accountMapper.getUidToEmail(account.getEmail());

        // salt 생성을 위한 날짜
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String salt = uid.toString() + calendar.getTime();

        salt = (BCrypt.hashpw(salt, BCrypt.gensalt()));
        accountMapper.setSalt(uid, salt);

        return new BaseResponse("회원가입에 성공했습니다.", HttpStatus.OK);
    }

    @Override
    public Map<String, Object> checkKey(String token) throws Exception {
        return jwt.verifyJWT(token);
    }

    @Override
    public Map<String, String> login(AccountDTO account) throws Exception {
        AccountDTO accountDTO = accountMapper.getAccountToEmail(account.getEmail());
        if (account.getEmail() == null)
            throw new Exception("이메일이 잘못되었습니다.");

        if (!BCrypt.checkpw(account.getPassword(), accountMapper.getPasswordToEmail(account.getEmail()))) {
            throw new Exception("비밀번호가 잘못되었습니다.");
        } else {
            Map<String, String> token = new HashMap<>();
            token.put("access_token", jwt.createToken(accountDTO));
            return token;
        }
    }

    @Override
    public Map<String, String> refresh(String token) throws Exception {
        Map<String, Object> data = jwt.verifyJWT(token);
        if (data == null)
            throw new Exception("토큰이 잘못되었습니다.");

        AccountDTO account = new AccountDTO();
        account.setUid(Long.parseLong(data.get("uid").toString()));
        account.setNickname(data.get("nickname").toString());

        account.setSalt(accountMapper.getSaltToUid(account.getUid()));

        Map<String, String> refresh_token = new HashMap<>();
        refresh_token.put("access_token", new Jwt().createToken(account));
        return refresh_token;
    }
}