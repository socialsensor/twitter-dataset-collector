package eu.socialsensor.twcollect.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

public class FileUtil {

	public static final String UTF8 = "UTF-8";
	
	// assume that input strings correspond to longs
	public static long[] convertStringToLongs(String[] tokens){
		long[] ids = new long[tokens.length];
		for (int i = 0; i < tokens.length; i++){
			ids[i] = Long.parseLong(tokens[i]);
		}
		return ids;
	}
	
	// read comma-separated list of tokens
	public static String[] readTokensFromFile(String file){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), UTF8));
			String line = reader.readLine();
			reader.close();
			if (line == null){
				return new String[0];
			} else {
				return line.split(",");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return new String[0];
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new String[0];
		} catch (IOException e){
			e.printStackTrace();
			if (reader != null){
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return new String[0];
		} 
	}
	
	public static Set<String> readIds(String file){
		Set<String> ids = new HashSet<String>();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), UTF8));
			String line = null;
			while ((line = reader.readLine())!= null){
				String[] parts = line.split("[\t\\s]");
				ids.add(parts[0]);
			}
			reader.close();
			return ids;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return ids;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return ids;
		} catch (IOException e){
			e.printStackTrace();
			if (reader != null){
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return ids;
		} 
	}
}
