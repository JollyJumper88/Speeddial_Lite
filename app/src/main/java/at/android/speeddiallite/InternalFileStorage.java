package at.android.speeddiallite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;

public class InternalFileStorage {

	public static InternalFileStorage wotf;

	private static String FILENAME = "ContactDataStorage";

	public static InternalFileStorage getInstance() {
		if (wotf == null)
			return new InternalFileStorage();
		return wotf;
	}

	public void saveObjectToFile(ContactDataStorage cds, Context context) {

		try {
			if (Main.DEBUG_DATA_STORAGE) {
				System.out.println("Saving ...");
				cds.printData();
			}
			FileOutputStream fos = context.openFileOutput(FILENAME,
					Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(cds);
			oos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public ContactDataStorage loadObjectFromFile(Context context) {
		try {
			// System.out.println("Loading ...");
			File file = context.getFileStreamPath(FILENAME);
			if (file.exists()) {

				FileInputStream fis = context.openFileInput(FILENAME);
				ObjectInputStream ois = new ObjectInputStream(fis);
				ContactDataStorage cds = new ContactDataStorage();
				cds = (ContactDataStorage) ois.readObject();
				ois.close();

				// here we do backwards compatibility stuff
				// copy all data from old storage to new one
				if (cds.lookupKey == null) {
					// System.out.println("key is null - create new object and copy data");

					ContactDataStorage cdsCopy = new ContactDataStorage();
					cdsCopy.setButtonIdList(cds.getButtonIdList());
					cdsCopy.setcallerIdList(cds.getcallerIdList());
					cdsCopy.setnameList(cds.getnameList());
					cdsCopy.setnumberList(cds.getnumberList());
					cdsCopy.getInitializedLookupList();

					// after we have copied all data we need to save
					saveObjectToFile(cdsCopy, context);

					// System.out.println("---------------new data ---------");

					cdsCopy.printData();
					return cdsCopy;

					// return this if we want a new data object.
					// return new ContactDataStorage();

				}

				return cds;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ContactDataStorage();
	}

	public boolean copyDataStorageToSD(Context context) {
		String appPath = context.getFilesDir().getPath();
		File source = new File(appPath, FILENAME);

		if (source.exists()) {

			System.out.println("exists");

			File root = Environment.getExternalStorageDirectory();
			if (root.canWrite()) {
				File folder = new File(root, "/SpeedDial/");
				folder.mkdirs();

				File destination = new File(folder, FILENAME);

				try {
					if (root.canWrite()) {

						InputStream src = new FileInputStream(source);
						OutputStream dst = new FileOutputStream(destination);
						// Copy the bits from instream to outstream
						byte[] buf = new byte[1024];
						int len;
						while ((len = src.read(buf)) > 0) {
							dst.write(buf, 0, len);
						}
						src.close();
						dst.close();

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return false;
	}
}
