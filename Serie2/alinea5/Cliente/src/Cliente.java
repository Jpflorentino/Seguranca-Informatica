import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Set;


public class Cliente {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, CertificateException, InvalidAlgorithmParameterException, UnrecoverableKeyException {


        KeyStore trustedStore = KeyStore.getInstance("PKCS12");
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");


        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        FileInputStream fileInputStreamPFX = new FileInputStream("Alice_1.pfx");
        clientStore.load(fileInputStreamPFX, "changeit".toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, "changeit".toCharArray());
        KeyManager[] kms = kmf.getKeyManagers();




        FileInputStream fileInputStreamCER = new FileInputStream("CA1.cer");
        Certificate certificate = certificateFactory.generateCertificate(fileInputStreamCER);
        trustedStore.load(null);
        trustedStore.setCertificateEntry("1",certificate);

        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustedStore);



        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kms, trustFactory.getTrustManagers(), null);

        SSLSocketFactory sslFactory = context.getSocketFactory();
        SSLSocket client = (SSLSocket) sslFactory.createSocket("www.secure-server.edu", 4433);

        client.startHandshake();

        String request = "GET / HTTP/1.1\r\n\r\n";
        OutputStream os = client.getOutputStream();
        os.write(request.getBytes());
        os.flush();

        InputStream is = client.getInputStream();
        int ch;
        while( (ch=is.read())!= -1)
            System.out.print((char)ch);

        SSLSession session = client.getSession();
        System.out.println(session.getCipherSuite());
        System.out.println(session.getPeerCertificates()[0]);

        client.close();
    }

}
