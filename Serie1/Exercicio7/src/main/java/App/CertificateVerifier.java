package App;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class CertificateVerifier {

    //Verificar certificados
    //Funcao adicional
    public static boolean checkCertificateRoot(Certificate certificate) throws NoSuchProviderException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        try {
            PublicKey key = certificate.getPublicKey();
            certificate.verify(key);
            return true;
        } catch (InvalidKeyException | SignatureException invalidKeyException) {
            return false;
        }
    }

    public static boolean verificarCadeiaCertificados(Certificate certificate) throws NoSuchAlgorithmException, CertificateException, NoSuchProviderException, InvalidKeyException, SignatureException, FileNotFoundException, InvalidAlgorithmParameterException, CertPathBuilderException {
        Scanner scan = new Scanner(System.in);

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Set<X509Certificate> intermediateSet = new HashSet<X509Certificate>();
        Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();

        //gets Intermidiate certificates
        System.out.println("Insert Intermediate .cer File.");
        System.out.println("(If no more Certificates Write \"Done\")");

        String intermediate;
        while(!(intermediate = scan.nextLine()).equals("Done")) {

            FileInputStream certIntermediate = new FileInputStream(intermediate);
            X509Certificate certificateIntermediate = (X509Certificate) certificateFactory.generateCertificate(certIntermediate);
            intermediateSet.add(certificateIntermediate);
        }

        //gets Root certificate
        System.out.println("Insert Root .cer File");
        String root = scan.nextLine();
        FileInputStream certRoot = new FileInputStream(root);
        X509Certificate certificateRoot = (X509Certificate) certificateFactory.generateCertificate(certRoot);

        if (!checkCertificateRoot(certificateRoot)) {
            return false;
        }

        //Parameters of PKIXBuilder
        trustAnchors.add(new TrustAnchor(certificateRoot, null));
        X509CertSelector certSelector = new X509CertSelector();
        certSelector.setCertificate((X509Certificate) certificate);

        //Call PKIXBuilder
        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, certSelector);
        pkixParams.setRevocationEnabled(false);

        //CertStore Parameters

        //Specify a list of intermediate certificates
        CertStore intermediateCertStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(intermediateSet));
        pkixParams.addCertStore(intermediateCertStore);

        // Build and verify the certification chain
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
        PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) builder.build(pkixParams);
        if (result != null) {
            return true;
        }
        return false;
    }
}
