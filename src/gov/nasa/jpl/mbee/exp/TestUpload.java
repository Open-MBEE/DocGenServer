package gov.nasa.jpl.mbee.exp;

import gov.nasa.jpl.mbee.mdserver.Utils;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
public class TestUpload {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File in = new File("/Users/dlam/Desktop/testzip");
		File out = new File("/Users/dlam/Desktop/testzip.zip");
		Utils.zipFolder(in, out);
		
		//FormDataMultiPart form = new FormDataMultiPart().field("form", new File("/Users/dlam/Desktop/test.csv"), MediaType.MULTIPART_FORM_DATA_TYPE);
		FormDataMultiPart form = new FormDataMultiPart();
		form.bodyPart(new FileDataBodyPart("filename", out));
		form.field("some key", "some value");
		form.field("requestId", "blah");
	    WebResource webResource = Client.create().resource("http://localhost:8080/testupload/");
	    String response = webResource.type(MediaType.MULTIPART_FORM_DATA).accept(MediaType.TEXT_PLAIN).post(String.class, form);//get(String.class);//post(String.class, form); //how to get around django's csrf thing
	    System.out.println(response);
	}

	

}
