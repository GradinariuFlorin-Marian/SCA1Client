

import java.security.*;
import java.util.Base64;

public class Main {
    final static int ServerPort = 6666;
    public static PrivateKey clientPrivateKey;
    public static PublicKey merchantPublicKey, clientPublicKey, paymentGatewayPublicKey;

    public static void main(String args[]) throws Exception {
        Utils.initializeKeys();
        Utils.getKeyFromFile();
        OrderObject oj = Utils.initialiseCardObject();
        String str = null, str2 = null;
        AESManager am = new AESManager();
        Utils.resolutionSub_Protocol(am, oj);
        try {
            String val = Base64.getEncoder().encodeToString(clientPublicKey.getEncoded());
            str = am.encryptWithStringKey(val.substring(0, 117));
            str2 = am.encryptWithStringKey(val.substring(117, 216));
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }


        ClientManager cm = new ClientManager();
        cm.startConnection("localhost");
        //1/2 of the encription is sent
        cm.sendMessage(str);

        //Message encrypted with public key of client
        //Rest of the RSA encription is sent and we receive an confirmation back
        String[] splitter = cm.sendMessage(str2).split(";");
        //Decrypted half and half
        String decryptHalf = am.decryptNew(splitter[0]);
        String decryptOtherHalf = am.decryptNew(splitter[1]);
        //Splittind with SID and signature of SID
        String[] splitter2 = (decryptHalf + decryptOtherHalf).split(";");
        if (SignatureManager.validateMessageSignature(merchantPublicKey, splitter2[0], splitter2[1])) {
            oj.setSID(splitter2[0]);
            String PI = oj.getCardDetails() + ";" + oj.getSID() + ";" + oj.getAmount() + ";" +
                    Base64.getEncoder().encodeToString(clientPublicKey.getEncoded()) + ";" + oj.getNC() + ";" + oj.getM();
            //PM With Encryption of Payment Gateway
            //Teste
            BankManager bm = new BankManager();
            if (bm.verifyAccount(oj.getCardN(), oj.getCardExp(), oj.getCCode())) {
                if (!bm.verifyAndUpdateBalance(oj.getCardN(), oj.getCardExp(), oj.getCCode(), Double.valueOf(oj.getAmount()))) {
                    System.out.println("Not enough money!!");
                    System.exit(1);
                }
            } else {
                System.out.println("Account not valid!");
                System.exit(1);
            }

            ///
            String PM = PI + "," + SignatureManager.getSignature(PI);
            StringBuilder PMEncrypted = new StringBuilder();
            int a = 0;
            while (true) {
                if (a + 117 >= PM.length()) {
                    PMEncrypted.append(am.encryptWithStringKeyPaymentGateway(PM.substring(a)));
                    break;
                } else {
                    PMEncrypted.append(am.encryptWithStringKeyPaymentGateway(PM.substring(a, a + 117))).append(";");
                    a += 117;
                }
            }
            //End of PM Encryption with Payment Gateway Public Key
            String PO = oj.getOrderDesc() + ";" + oj.getSID() + ";" + oj.getAmount() + ";" + oj.getNC() + ";"
                    + SignatureManager.getSignature(oj.getAmount() + ";" + oj.getSID() + ";" + oj.getAmount() + ";" + oj.getNC());
            String add = PMEncrypted + "," + PO;

            for (int i = 0; i < (Math.ceil(add.length() / 117.0)); i++) {
                if (117 * (i + 1) <= add.length()) {
                    cm.sendMessage(am.encryptWithStringKey(add.substring(117 * i, 117 * (i + 1))));
                } else {
                    cm.sendMessage(am.encryptWithStringKey(add.substring(117 * i)));
                }
            }
            System.out.println("Raw from C to M:" + add);
        } else {
            System.out.println("Wrong signature! Client closed!");
            System.exit(1);
        }
    }

}
