package domain.dto;

public class AuthDTO {
    private Long uid;

    public AuthDTO(Long uid, String nickname) {
        this.uid = uid;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }
}