package fileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class StoreInDirectory {

	private String subject;
	private String lecture;

	File propFile = new File("config.properties");
	Properties properties = new Properties();
	private Scanner scn;

	public StoreInDirectory(String moduleName, String lecNumber) {
		subject = moduleName;
		lecture = lecNumber;
	}

	/**
	 * Promotes to enter path
	 * 
	 * @return return entered path
	 */
	public String askForPath() {
		scn = new Scanner(System.in);
		System.out.println("Enter where you want to store your dir: ");
		String pathName = scn.nextLine();
		return pathName;
	}

	/**
	 * Check if file exists
	 * 
	 * @param fl
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public boolean fileExists(File fl) throws FileNotFoundException,
			IOException {

		String filePathString = getPath("lecture") + "\\" + fl.getName();

		File f = new File(filePathString);

		if (f.exists()) {
			return true;
		}

		return false;
	}

	public void createRootFolder(String dirPath) {
		File theDir = new File(dirPath + "\\" + "FILESPACE");
		if (!theDir.exists()) {
			System.out.println("Create directory: " + dirPath + "\\"
					+ "FILESPACE");
			theDir.mkdir();
		}
	}

	public void createSubject(String path) {
		File theDir = new File(path + "\\" + subject);
		theDir.mkdir();
	}

	/**
	 * 
	 * @param path
	 */
	public void createLecture(String path) {
		File theDir = new File(path + "\\" + "Lecture " + lecture);
		theDir.mkdir();

	}

	/**
	 * 
	 * @param path
	 *            - directory path
	 * @param fin
	 *            - file to be inserted
	 * @throws IOException
	 */
	public void insertFile(String path, File fin) throws IOException {

		File f = new File(path + "\\" + "Lecture " + lecture + "\\" + fin);
		if (!f.exists()) {
			f.createNewFile();
		} else {
			System.out.println("not created");
		}

	}

	/**
	 * Creates a new file directory in properties file
	 * 
	 * @throws IOException
	 */
	public void newFileDirectory() throws IOException {
		Properties properties = new Properties();
		String path = askForPath();
		properties.setProperty("FileDirectory", path);
		FileWriter writer = new FileWriter("config.properties");
		properties.store(writer, "Author: Lync");
		writer.close();
	}

	/**
	 * 
	 * @param type
	 *            - string input "module" or "lecture"
	 * @return with stated parameters return path to specified folder else
	 *         return path only to root folder
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String getPath(String type) throws FileNotFoundException,
			IOException {
		String path = null;
		Properties properties = new Properties();
		properties.load(new FileInputStream("config.properties"));
		path = properties.getProperty("FileDirectory");
		if (type == "module") {
			path = path + subject + "\\";
		} else if (type.equals("lecture")) {
			path = path + subject + "\\" + "Lecture " + lecture + "\\";
		}
		return path;
	}

	/**
	 * 
	 * @return the path in the properties file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String getFullPath() throws FileNotFoundException, IOException {
		String path = null;
		Properties properties = new Properties();
		properties.load(new FileInputStream("config.properties"));
		path = properties.getProperty("FileDirectory");

		return path;
	}

	/**
	 * Check if the tag in property file exists
	 * 
	 * @return - true is exists
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public boolean pathExists() throws FileNotFoundException, IOException {

		if (propFile.exists()) {
			properties.load(new FileInputStream("config.properties"));

			if (properties.getProperty("FileDirectory") != null) {
				return false;
			} else {
				return true;
			}

		}

		return true;
	}

	/**
	 * 
	 * @param KeepDir
	 *            - Switch between write to property file or place file to
	 *            folder
	 * @param fl
	 *            - the file to insert
	 * @throws IOException
	 */
	public void createProperty(boolean KeepDir, File fl) throws IOException {

		if (propFile.exists()) {

			properties.load(new FileInputStream("config.properties"));

			if (properties.getProperty("FileDirectory") != null) {
				if (KeepDir == true) {
					// System.out.println(properties.getProperty("FileDirectory"));
					// System.out.println("we are in");
					createRootFolder(properties.getProperty("FileDirectory"));

					createSubject(properties.getProperty("FileDirectory")
							.toString() + "\\FILESPACE");

					createLecture(properties.getProperty("FileDirectory")
							.toString() + "\\FILESPACE\\" + subject);

					insertFile(properties.getProperty("FileDirectory")
							.toString() + "\\FILESPACE\\" + subject, fl);

				} else {
					newFileDirectory();
				}
			} else {
				newFileDirectory();
			}
		} else {
			newFileDirectory();
		}
	}

	/*
	 * 
	 * 
	 * public static void main(String[] args) throws IOException {
	 * 
	 * String subject = "Machine Learning"; String LectureNum = "6";
	 * 
	 * CreateFolder create = new CreateFolder(subject, LectureNum);
	 * 
	 * //create.createProperty(false); create.createProperty(true);
	 * 
	 * System.out.println(create.getPath("module"));
	 * 
	 * 
	 * 
	 * }
	 */
}
