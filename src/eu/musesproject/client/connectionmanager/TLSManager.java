/*
 * version 1.0 - MUSES prototype software
 * Copyright MUSES project (European Commission FP7) - 2013 
 * 
 */

package eu.musesproject.client.connectionmanager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.os.Environment;

/**
 * Handle TLS/SSL communication with the server
 * 
 * @author Yasir Ali
 * @version Jan 27, 2014
 */

public class TLSManager {

	public TLSManager() {

	}

	/**
	 * Get HttpsClient object
	 * @return DefaultHttpClient
	 */
	
	public DefaultHttpClient getTLSHttpClient(){
		return (DefaultHttpClient) createHttpClient();
	}

	/**
	 * Create SSLFactory object using certificate saved in the device
	 * @return SSLSocketFactory
	 */
	
	private SSLSocketFactory newSslSocketFactory() {
		try {
			String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
			String certificateName = "localhost.crt";
			InputStream in = new BufferedInputStream(new FileInputStream(baseDir + File.separator + certificateName));
			KeyStore trustedStore = null;

			if (in != null){
				trustedStore = ConvertCerToBKS(in, "muses alias", "muses11".toCharArray());
			}
			SSLSocketFactory sf = new SSLSocketFactory(trustedStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			return sf;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	
	
	/**
	 * Convert local certificate to BKS
	 * @param cerStream
	 * @param alias
	 * @param password
	 * @return keyStore
	 */
	private KeyStore ConvertCerToBKS(InputStream cerStream, String alias, char [] password){
	    KeyStore keyStore = null;
	    try {
	        keyStore = KeyStore.getInstance("BKS", "BC");
	        CertificateFactory factory = CertificateFactory.getInstance("X.509", "BC");
	        Certificate certificate = factory.generateCertificate(cerStream);
	        keyStore.load(null, password);
	        keyStore.setCertificateEntry(alias, certificate);
	    }
	    catch (Exception e){
	    	System.out.println(e.getLocalizedMessage());
	    }
	    return keyStore;                                    
	}
	/**
	 * Sreate https client object
	 * @return DefaultHttpClient
	 */
	private HttpClient createHttpClient() {
	    HttpParams params = new BasicHttpParams();
	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	    HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
	    HttpProtocolParams.setUseExpectContinue(params, true);

	    SchemeRegistry schReg = new SchemeRegistry();
	    schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    schReg.register(new Scheme("https", (SocketFactory) newSslSocketFactory(), 8443));
	    ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

	    return new DefaultHttpClient(conMgr, params);
	}
	
}







