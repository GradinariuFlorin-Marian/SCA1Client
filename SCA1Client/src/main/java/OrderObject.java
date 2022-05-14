import lombok.Getter;
import lombok.Setter;

import java.security.PublicKey;

@Getter
@Setter
public class OrderObject {
    private String cardN, cardExp, cCode, Amount, NC, M, orderDesc, SID;
    private PublicKey clientPublicKey;

    public String getCardDetails(){
        return cardN + ";" + cardExp + ";" + cCode;
    }
}
