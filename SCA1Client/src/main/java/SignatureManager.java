import java.security.*;
import java.util.Base64;

public class SignatureManager {
    public static boolean validateMessageSignature(PublicKey publicKey, String message, String signature) throws
            NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature clientSig = Signature.getInstance("SHA256withRSA");
        clientSig.initVerify(publicKey);
        clientSig.update(message.getBytes());
        byte[] signatureBytes = Base64.getDecoder().decode(signature);
        if (clientSig.verify(signatureBytes)) {
            return true;
        } else {
            return false;
        }
    }
    public static String getSignature(String message) {
        StringBuilder sb = new StringBuilder();
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(Main.clientPrivateKey);
            sign.update(message.getBytes());
            byte[] signature = sign.sign();
            sb.append(Base64.getEncoder().encodeToString(signature));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
