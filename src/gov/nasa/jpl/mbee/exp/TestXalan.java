package gov.nasa.jpl.mbee.exp;
/*
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;

public class TestXalan {
	public static void main(String[] args) {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tFactory.newTransformer(new StreamSource(new File("/Users/dlam/Desktop/wordml.xsl")));
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		

		transformer.setParameter("template", "/Users/dlam/Desktop/template.doc");
		//transformer.setOutputProperty("encoding", "ISO-8859-1");
		FileOutputStream out = null;
		try {
			out = new FileOutputStream("/Users/dlam/Desktop/word.doc");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			StreamResult res = new StreamResult(out);
			transformer.transform(new StreamSource(new File("/Users/dlam/Desktop/out.xml")), res);
		} catch (TransformerException e) {
			System.out.println("transformer exception");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		  //Clean-up
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
*/