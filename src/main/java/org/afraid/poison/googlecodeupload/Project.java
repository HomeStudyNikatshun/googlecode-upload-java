package org.afraid.poison.googlecodeupload;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Andreas Schnaiter <rc.poison@gmail.com>
 */
@XmlRootElement
public class Project {

	private final static String GOOGLECODE=".googlecode.com";
	private String userName;
	private String password;
	private String projectName;
	private boolean verbose=false;
	private boolean ignoreSslCertificateHostname=false;

	public Project() {
	}

	public Project(String userName, String password, String projectName) {
		this.userName=userName;
		this.password=password;
		this.projectName=projectName;
	}

	public boolean isIgnoreSslCertificateHostname() {
		return ignoreSslCertificateHostname;
	}

	public void setIgnoreSslCertificateHostname(boolean ignoreSslCertificateHostname) {
		this.ignoreSslCertificateHostname=ignoreSslCertificateHostname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password=password;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName=projectName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName=userName;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose=verbose;
	}

	private static void log(String msg) {
		System.err.println(msg);
	}

	private static Logger logger() {
		return Logger.getLogger("googlecode-upload");
	}

	private static void sendLine(OutputStream out, String string) throws IOException {
		out.write(string.getBytes("ascii"));
		out.write("\r\n".getBytes("ascii"));
	}

	private String createAuthToken() throws UnsupportedEncodingException {
		String tokenString=getUserName()+":"+getPassword();
		return Base64.encodeBase64URLSafeString(tokenString.getBytes("UTF-8"));
	}

	public void upload(FileDefinition fileDefinition) throws MalformedURLException, FileNotFoundException, IOException {
		System.clearProperty("javax.net.ssl.trustStoreType");
		System.clearProperty("javax.net.ssl.trustStoreProvider");

		final String BOUNDARY="IMustSayIHaveToSetSomeBoundariesHere___";
		URL url=createUploadURL();
		logger().log(Level.INFO, "upload URL: {0}", url);

		InputStream in=new BufferedInputStream(new FileInputStream(fileDefinition.getFile()));

		HttpURLConnection conn=(HttpURLConnection) url.openConnection();
		if (this.isIgnoreSslCertificateHostname()) {
			if (conn instanceof HttpsURLConnection) {
				HttpsURLConnection secure=(HttpsURLConnection) conn;
				secure.setHostnameVerifier(new HostnameVerifier() {

					@Override
					public boolean verify(String hostname, SSLSession session) {
						boolean result=true;
						logger().info("ignoring SSL verification");
						return result;
					}
				});
			}
		}

		conn.setDoOutput(true);
		conn.setRequestProperty("Authorization", "Basic "+createAuthToken());
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+BOUNDARY);
		conn.setRequestProperty("User-Agent", "org.afraid.poison.googlecode-upload");

		logger().log(Level.INFO, "Connecting (username: {0})", userName);
		conn.connect();

		logger().info("Sending request parameters");
		OutputStream out=conn.getOutputStream();
		sendLine(out, "--"+BOUNDARY);
		sendLine(out, "content-disposition: form-data; name=\"summary\"");
		sendLine(out, "");
		sendLine(out, fileDefinition.getSummary());

		if (fileDefinition.getLabels()!=null&&!fileDefinition.getLabels().isEmpty()) {
			logger().log(Level.INFO, "Setting labels: {0}", fileDefinition.getLabels());

			for (String label : fileDefinition.getLabels()) {
				sendLine(out, "--"+BOUNDARY);
				sendLine(out, "content-disposition: form-data; name=\"label\"");
				sendLine(out, "");
				sendLine(out, label.trim());
			}
		}

		log("Sending file: "+fileDefinition.getTargetFileName());
		sendLine(out, "--"+BOUNDARY);
		sendLine(out, "content-disposition: form-data; name=\"filename\"; filename=\""+fileDefinition.getTargetFileName()+"\"");
		sendLine(out, "Content-Type: application/octet-stream");
		sendLine(out, "");
		int count;
		byte[] buf=new byte[16384];
		while ((count=in.read(buf))>=0) {
			out.write(buf, 0, count);
		}
		in.close();
		sendLine(out, "");
		sendLine(out, "--"+BOUNDARY+"--");

		out.flush();
		out.close();
		in=conn.getInputStream();

		logger().info("upload finished");

		logger().log(Level.INFO, "HTTP Response Headers: {0}", conn.getHeaderFields());
		StringBuilder responseBody=new StringBuilder();
		while ((count=in.read(buf))>=0) {
			responseBody.append(new String(buf, 0, count, "ascii"));
		}
		log(responseBody.toString());
		in.close();

		conn.disconnect();
	}

	private URL createUploadURL() throws MalformedURLException {
		if (getProjectName()==null) {
			throw new NullPointerException("projectName required");
		}
		return new URL("https", getProjectName()+GOOGLECODE, "/files");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj==null) {
			return false;
		}
		if (getClass()!=obj.getClass()) {
			return false;
		}
		final Project other=(Project) obj;
		if ((this.userName==null) ? (other.userName!=null) : !this.userName.equals(other.userName)) {
			return false;
		}
		if ((this.password==null) ? (other.password!=null) : !this.password.equals(other.password)) {
			return false;
		}
		if ((this.projectName==null) ? (other.projectName!=null) : !this.projectName.equals(other.projectName)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash=7;
		hash=67*hash+(this.userName!=null ? this.userName.hashCode() : 0);
		hash=67*hash+(this.password!=null ? this.password.hashCode() : 0);
		hash=67*hash+(this.projectName!=null ? this.projectName.hashCode() : 0);
		return hash;
	}
}
