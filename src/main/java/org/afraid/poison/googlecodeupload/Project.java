package org.afraid.poison.googlecodeupload;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Andreas Schnaiter <rc.poison@gmail.com>
 */
@XmlRootElement
public class Project {

	private final static String GOOGLECODE=".googlecode.com";
	private String userName;
	private String password;
	private String name;
	private boolean verbose=false;
	private boolean ignoreSslCertificateHostname=false;

	public Project() {
	}

	public Project(String userName, String password, String name) {
		this.userName=userName;
		this.password=password;
		this.name=name;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name=name;
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

	private static Logger logger() {
		return Logger.getLogger("googlecode-upload");
	}

	public void upload(FileDefinition fileDefinition) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException, IOException {
		DefaultHttpClient httpclient=new DefaultHttpClient();
		try {
			URL googlecodeUrl=createUploadURL();
			
			httpclient.getCredentialsProvider().setCredentials(
					new AuthScope(googlecodeUrl.getHost(), 443),
					new UsernamePasswordCredentials(getUserName(), getPassword()));
			HttpPost httppost=new HttpPost(googlecodeUrl.toURI());

			MultipartEntity reqEntity=new MultipartEntity();
			FileBody bin=new FileBody(fileDefinition.getFile());
			reqEntity.addPart("bin", bin);
			StringBody summary=new StringBody(fileDefinition.getSummary());
			reqEntity.addPart("summary", summary);
			StringBody fileName=new StringBody(fileDefinition.getTargetFileName());
			reqEntity.addPart("filename", fileName);
			if (fileDefinition.getLabels()!=null&&!fileDefinition.getLabels().isEmpty()) {
				for (String label : fileDefinition.getLabels()) {
					StringBody lBody=new StringBody(label);
					reqEntity.addPart("label", lBody);
				}
			}

			httppost.setEntity(reqEntity);

			System.out.println("executing request "+httppost.getRequestLine());
			HttpResponse response=httpclient.execute(httppost);
			HttpEntity resEntity=response.getEntity();
			System.out.println("----------------------------------------");
			System.out.println(response.getStatusLine());
			if (null!=resEntity) {
				System.out.println("Response content length: "+resEntity.getContentLength());
			}
			EntityUtils.consume(resEntity);
		} finally {
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception ignore) {
			}
		}
	}

	private URL createUploadURL() throws MalformedURLException {
		if (getName()==null) {
			throw new NullPointerException("projectName required");
		}
		return new URL("https", getName()+GOOGLECODE, "/files");
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
		if ((this.name==null) ? (other.name!=null) : !this.name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash=7;
		hash=67*hash+(this.userName!=null ? this.userName.hashCode() : 0);
		hash=67*hash+(this.password!=null ? this.password.hashCode() : 0);
		hash=67*hash+(this.name!=null ? this.name.hashCode() : 0);
		return hash;
	}
}
