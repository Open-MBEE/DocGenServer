package gov.nasa.jpl.mbee.mdserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

public class Utils {

	public static void zipFolder(File folderToBeZipped, File zipfile) throws IOException {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
		int len = folderToBeZipped.getAbsolutePath().lastIndexOf(File.separator);
		String baseName = folderToBeZipped.getAbsolutePath().substring(0, len+1);
		addFolderToZip(folderToBeZipped, out, baseName);
		out.flush();
		out.close();
	}
	
	private static void addFolderToZip(File folder, ZipOutputStream zip, String baseName) throws IOException {
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				addFolderToZip(file, zip, baseName);
			} else {
				String name = file.getAbsolutePath().substring(baseName.length());
				ZipEntry zipEntry = new ZipEntry(name);
				zip.putNextEntry(zipEntry);
				IOUtils.copy(new FileInputStream(file), zip);
				zip.closeEntry();
			}
		}
	}
	
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }
	    return dir.delete();
	}
}
