/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.network;

import com.dropbox.core.util.IOUtil;
import com.dropbox.core.util.LangUtil;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SSLConfig {
    private static final SSLSocketFactory sslSocketFactory = createSSLSocketFactory();
    private static final String[] protocolListTLS_v1_2 = new String[]{"TLSv1.2"};
    private static final String[] protocolListTLS_v1_0 = new String[]{"TLSv1.0"};
    private static final String[] protocolListTLS_v1 = new String[]{"TLSv1"};
    private static HashSet<String> allowedCipherSuites = new HashSet(Arrays.asList(new String[]{"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_RC4_128_SHA", "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_GCM_SHA384", "TLS_RSA_WITH_AES_256_CBC_SHA256", "TLS_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA", "ECDHE-RSA-AES256-GCM-SHA384", "ECDHE-RSA-AES256-SHA384", "ECDHE-RSA-AES256-SHA", "ECDHE-RSA-AES128-GCM-SHA256", "ECDHE-RSA-AES128-SHA256", "ECDHE-RSA-AES128-SHA", "ECDHE-RSA-RC4-SHA", "DHE-RSA-AES256-GCM-SHA384", "DHE-RSA-AES256-SHA256", "DHE-RSA-AES256-SHA", "DHE-RSA-AES128-GCM-SHA256", "DHE-RSA-AES128-SHA256", "DHE-RSA-AES128-SHA", "AES256-GCM-SHA384", "AES256-SHA256", "AES256-SHA", "AES128-GCM-SHA256", "AES128-SHA256", "AES128-SHA"}));
    private static SSLConfig.CipherSuiteFilterationResults cachedCipherSuiteFilterationResults;
    private static final String RootCertsResourceName = "trusted-certs.raw";
    public static final int MaxCertLength = 10240;

    public SSLConfig() {
    }

    public static void apply(HttpsURLConnection conn) throws SSLException {
        conn.setSSLSocketFactory(sslSocketFactory);
    }

    public static SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }

    private static void limitProtocolsAndCiphers(SSLSocket socket) throws SSLException {
        String[] arr$ = socket.getSupportedProtocols();
        int len$ = arr$.length;
        int i$ = 0;

        while (true) {
            if (i$ >= len$) {
                throw new SSLException("Socket doesn\'t support protocols \"TLSv1.2\", \"TLSv1.0\" or \"TLSv1\".");
            }

            String protocol = arr$[i$];
            if (protocol.equals("TLSv1.2")) {
                socket.setEnabledProtocols(protocolListTLS_v1_2);
                break;
            }

            if (protocol.equals("TLSv1.0")) {
                socket.setEnabledProtocols(protocolListTLS_v1_0);
                break;
            }

            if (protocol.equals("TLSv1")) {
                socket.setEnabledProtocols(protocolListTLS_v1);
                break;
            }

            ++i$;
        }

        socket.setEnabledCipherSuites(getFilteredCipherSuites(socket.getSupportedCipherSuites()));
    }

    private static String[] getFilteredCipherSuites(String[] supportedCipherSuites) {
        SSLConfig.CipherSuiteFilterationResults cached = cachedCipherSuiteFilterationResults;
        if (cached != null && Arrays.equals(cached.supported, supportedCipherSuites)) {
            return cached.enabled;
        } else {
            ArrayList enabled = new ArrayList(allowedCipherSuites.size());
            String[] filteredArray = supportedCipherSuites;
            int len$ = supportedCipherSuites.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                String supported = filteredArray[i$];
                if (allowedCipherSuites.contains(supported)) {
                    enabled.add(supported);
                }
            }

            filteredArray = (String[]) enabled.toArray(new String[enabled.size()]);
            cachedCipherSuiteFilterationResults = new SSLConfig.CipherSuiteFilterationResults(supportedCipherSuites, filteredArray);
            return filteredArray;
        }
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        KeyStore trustedCertKeyStore = loadKeyStore("trusted-certs.raw");
        TrustManager[] trustManagers = createTrustManagers(trustedCertKeyStore);
        SSLContext sslContext = createSSLContext(trustManagers);
        return new SSLConfig.SSLSocketFactoryWrapper(sslContext.getSocketFactory());
    }

    private static SSLContext createSSLContext(TrustManager[] trustManagers) {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException var4) {
            throw LangUtil.mkAssert("Couldn\'t create SSLContext", var4);
        }

        try {
            sslContext.init((KeyManager[]) null, trustManagers, (SecureRandom) null);
            return sslContext;
        } catch (KeyManagementException var3) {
            throw LangUtil.mkAssert("Couldn\'t initialize SSLContext", var3);
        }
    }

    private static TrustManager[] createTrustManagers(KeyStore trustedCertKeyStore) {
        TrustManagerFactory tmf;
        try {
            tmf = TrustManagerFactory.getInstance("X509");
        } catch (NoSuchAlgorithmException var4) {
            throw LangUtil.mkAssert("Unable to create TrustManagerFactory", var4);
        }

        try {
            tmf.init(trustedCertKeyStore);
        } catch (KeyStoreException var3) {
            throw LangUtil.mkAssert("Unable to initialize TrustManagerFactory with key store", var3);
        }

        return tmf.getTrustManagers();
    }

    private static KeyStore loadKeyStore(String certFileResourceName) {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] in = new char[0];
            keyStore.load((InputStream) null, in);
        } catch (KeyStoreException var17) {
            throw LangUtil.mkAssert("Couldn\'t initialize KeyStore", var17);
        } catch (CertificateException var18) {
            throw LangUtil.mkAssert("Couldn\'t initialize KeyStore", var18);
        } catch (NoSuchAlgorithmException var19) {
            throw LangUtil.mkAssert("Couldn\'t initialize KeyStore", var19);
        } catch (IOException var20) {
            throw LangUtil.mkAssert("Couldn\'t initialize KeyStore", var20);
        }

        InputStream in1 = SSLConfig.class.getResourceAsStream(certFileResourceName);
        if (in1 == null) {
            throw new AssertionError("Couldn\'t find resource \"" + certFileResourceName + "\"");
        } else {
            try {
                loadKeyStore(keyStore, in1);
            } catch (KeyStoreException var13) {
                throw LangUtil.mkAssert("Error loading from \"" + certFileResourceName + "\"", var13);
            } catch (SSLConfig.LoadException var14) {
                throw LangUtil.mkAssert("Error loading from \"" + certFileResourceName + "\"", var14);
            } catch (IOException var15) {
                throw LangUtil.mkAssert("Error loading from \"" + certFileResourceName + "\"", var15);
            } finally {
                IOUtil.closeInput(in1);
            }

            return keyStore;
        }
    }

    private static void loadKeyStore(KeyStore keyStore, InputStream in) throws IOException, SSLConfig.LoadException, KeyStoreException {
        CertificateFactory x509CertFactory;
        try {
            x509CertFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException var11) {
            throw LangUtil.mkAssert("Couldn\'t initialize X.509 CertificateFactory", var11);
        }

        DataInputStream din = new DataInputStream(in);
        byte[] data = new byte[10240];

        while (true) {
            int length = din.readUnsignedShort();
            if (length == 0) {
                if (din.read() >= 0) {
                    throw new SSLConfig.LoadException("Found data after after zero-length header.");
                }

                return;
            }

            if (length > 10240) {
                throw new SSLConfig.LoadException("Invalid length for certificate entry: " + length);
            }

            din.readFully(data, 0, length);

            X509Certificate cert;
            try {
                cert = (X509Certificate) x509CertFactory.generateCertificate(new ByteArrayInputStream(data, 0, length));
            } catch (CertificateException var10) {
                throw new SSLConfig.LoadException("Error loading certificate: " + var10.getMessage());
            }

            String alias = cert.getSubjectX500Principal().getName();

            try {
                keyStore.setCertificateEntry(alias, cert);
            } catch (KeyStoreException var9) {
                throw new SSLConfig.LoadException("Error loading certificate: " + var9.getMessage());
            }
        }
    }

    public static final class LoadException extends Exception {
        public LoadException(String message) {
            super(message);
        }
    }

    private static final class SSLSocketFactoryWrapper extends SSLSocketFactory {
        private final SSLSocketFactory mBase;

        public SSLSocketFactoryWrapper(SSLSocketFactory base) {
            this.mBase = base;
        }

        public String[] getDefaultCipherSuites() {
            return this.mBase.getDefaultCipherSuites();
        }

        public String[] getSupportedCipherSuites() {
            return this.mBase.getSupportedCipherSuites();
        }

        public Socket createSocket(String host, int port) throws IOException {
            Socket socket = this.mBase.createSocket(host, port);
            SSLConfig.limitProtocolsAndCiphers((SSLSocket) socket);
            return socket;
        }

        public Socket createSocket(InetAddress host, int port) throws IOException {
            Socket socket = this.mBase.createSocket(host, port);
            SSLConfig.limitProtocolsAndCiphers((SSLSocket) socket);
            return socket;
        }

        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            Socket socket = this.mBase.createSocket(host, port, localHost, localPort);
            SSLConfig.limitProtocolsAndCiphers((SSLSocket) socket);
            return socket;
        }

        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            Socket socket = this.mBase.createSocket(address, port, localAddress, localPort);
            SSLConfig.limitProtocolsAndCiphers((SSLSocket) socket);
            return socket;
        }

        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            Socket socket = this.mBase.createSocket(s, host, port, autoClose);
            SSLConfig.limitProtocolsAndCiphers((SSLSocket) socket);
            return socket;
        }
    }

    private static final class CipherSuiteFilterationResults {
        public final String[] supported;
        public final String[] enabled;

        private CipherSuiteFilterationResults(String[] supported, String[] enabled) {
            this.supported = supported;
            this.enabled = enabled;
        }
    }
}
