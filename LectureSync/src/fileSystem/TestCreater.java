package fileSystem;

import java.io.File;
import java.io.IOException;

public class TestCreater {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		String subject = "Machine Learning";
		String lectureNum = null;
		String vary = null;

		for (int i = 1; i <= 12; i++) {

			for (int j = 1; j <= 6; j++) {

				vary = String.valueOf(j);
				// vary = "1";

				lectureNum = String.valueOf(i);

				File file = new File("sampleFile" + vary + ".txt");

				StoreInDirectory store = new StoreInDirectory(subject,
						lectureNum);

				if (i == 1 && j == 1) {
					store.createProperty(false, file);
				}

				if (store.fileExists(file) == true) {
					System.out.println("File aleady exist");

				} else {
					store.createProperty(true, file);
				}

				System.in.read();
				// System.out.println(store.getPath("lecture"));

			}

		}

	}

}
