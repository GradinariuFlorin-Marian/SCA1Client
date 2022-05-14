

import javax.crypto.Cipher;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Utils {

    public static void selectOption(DataOutputStream dos) {

        System.out.println("Please type: \nBuy \nQuit");
        System.out.print("Type: ");
        Scanner sc = new Scanner(System.in);
        switch (sc.nextLine()) {
            case "auth":
                StringBuilder sb = new StringBuilder(); //PM
                Scanner sc1 = new Scanner(System.in);
                sb.append(sc1.nextLine()).append(";"); //Card Number
                sc1 = new Scanner(System.in);
                sb.append(sc1.nextLine()).append(";"); //Card Expiration
                sc1 = new Scanner(System.in);
                sb.append(sc1.nextLine()).append(";"); //Code verification length 3
                sb.append("112").append(";"); //SID from Merchant
                sb.append(2).append(";"); //Amount
                sb.append(sc1.nextLine()).append(";"); //Public Key
                sb.append("12345").append(";"); //NC from Merchant
                sb.append("12").append(";"); //M

                StringBuilder sb2 = new StringBuilder(); //PO
                sb2.append("Descriere comanda").append(";"); //OrderDesc
                sb2.append("112").append(";"); //Sid
                sb2.append(2).append(";"); //Amount
                sb2.append("12345").append(";"); //NC from Merchant

                //User //Pass //Code
                break;
            case "quit":
                System.out.println("Client interface closed!");
                try {
                    dos.writeUTF("logout");
                    System.exit(1);
                } catch (IOException e) {
                    System.out.println("Payment Gateway Closed! Transaction canceled!");
                    System.exit(1);
                }
                break;
            default:
                System.out.println("Wrong option!");
                selectOption(dos);
        }
    }

    public static void initializeKeys() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.generateKeyPair();
            Main.clientPublicKey = kp.getPublic();
            Main.clientPrivateKey = kp.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void getKeyFromFile() {
        try {
            //Merchant Key
            Path path = Paths.get(ClassLoader.getSystemResource("MerchantKey.pub").toURI());
            byte[] bytes = Files.readAllBytes(path);

            /* Generate public key. */
            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            Main.merchantPublicKey = kf.generatePublic(ks);

            Path path2 = Paths.get(ClassLoader.getSystemResource("PaymentGatewayKey.pub").toURI());
            byte[] bytes2 = Files.readAllBytes(path2);

            /* Generate public key. */
            X509EncodedKeySpec ks2 = new X509EncodedKeySpec(bytes2);
            Main.paymentGatewayPublicKey = kf.generatePublic(ks2);
        } catch (Exception e) {
            System.out.println("Key not found!");
        }
    }

    public static OrderObject initialiseCardObject() {
        OrderObject oj = new OrderObject();
        oj.setCardN("4321123487641234");
        oj.setCardExp("03/26");
        oj.setCCode("738");
        oj.setAmount("7");
        oj.setNC("83");
        oj.setM("123");
        oj.setOrderDesc("Order description");
        return oj;
    }

    public static String resolutionSub_Protocol(AESManager am, OrderObject oo) throws Exception {
        String RSP = oo.getSID() + ";" + oo.getAmount() + ";" + oo.getNC() + ";" + Base64.getEncoder().encodeToString(Main.clientPublicKey.getEncoded()) +
                ";" + SignatureManager.getSignature(oo.getSID() + "," + oo.getAmount() + "," + oo.getNC()
                + "," + Base64.getEncoder().encodeToString(Main.clientPublicKey.getEncoded()));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (Math.ceil(RSP.length() / 117.0)); i++) {
            if (117 * (i + 1) < RSP.length()) {
                sb.append(am.encryptWithStringKeyPaymentGateway(RSP.substring(117 * i, 117 * (i + 1))));
            } else {
                sb.append(am.encryptWithStringKeyPaymentGateway(RSP.substring(117 * i)));
            }
        }
        return sb.toString();
    }
}
