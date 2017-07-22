package at.android.speeddiallite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ContactDataStorage implements Serializable {

	private static final long serialVersionUID = -2253102983375664687L;
	private List<String> buttonId = new ArrayList<String>();
	private List<String> callerId = new ArrayList<String>();
	private List<String> number = new ArrayList<String>();
	private List<String> name = new ArrayList<String>();
	public List<String> lookupKey = new ArrayList<String>();

	// private Map<Integer, String> alternativeNumber = new HashMap<Integer,
	// String>();
	// private Map<Integer, String> data = new HashMap<Integer, String>();

	public ContactDataStorage() {

	}

	/**
	 * Contact Data Storage: If data row already exists, delete it and add
	 * current as new row
	 * 
	 */
	protected void addDataRow(String buttonId, String callerId, String number,
			String name, String lookupKey) {

		try {
			int currentIndex = getIndexByButtonId(Integer.parseInt(buttonId));
			if (currentIndex != -1) {
				removeDataRow(currentIndex);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.buttonId.add(buttonId);
		this.callerId.add(callerId);
		this.number.add(number);
		this.name.add(name);
		this.lookupKey.add(lookupKey);
		// System.out.println("Adding new Contact entry...");
	}

	private void removeDataRow(int currentIndex) {
		this.buttonId.remove(currentIndex);
		this.callerId.remove(currentIndex);
		this.number.remove(currentIndex);
		this.name.remove(currentIndex);
		this.lookupKey.remove(currentIndex);
		// System.out.println("Removing existing Contact entry...");
	}

	protected void removeDataRowByButtonId(int buttonId) {
		int currentIndex = this.getIndexByButtonId(buttonId);
		if (currentIndex != -1)
			this.removeDataRow(currentIndex);
	}

	protected String getNumberByButtonId(int buttonId) {
		int index = getIndexByButtonId(buttonId);
		if (index < number.size() && index != -1)
			return number.get(index);
		return null;
	}

	protected String getNameByButtonId(int buttonId) {
		int index = getIndexByButtonId(buttonId);
		if (index < name.size() && index != -1)
			return name.get(index);
		return null;
	}

	protected String getCallerIdByButtonId(int buttonId) {
		int index = getIndexByButtonId(buttonId);
		if (index < callerId.size() && index != -1) {
			return callerId.get(index);
		}
		return null;
	}

	protected String getLookupKeyByButtonId(int buttonId) {
		int index = getIndexByButtonId(buttonId);
		if (index < lookupKey.size() && index != -1) {
			return lookupKey.get(index);
		}
		return null;
	}

	protected int getIndexByButtonId(int buttonId) {
		ListIterator<String> iter = this.buttonId.listIterator();

		int index = 0;
		while (iter.hasNext()) {
			int i = Integer.parseInt(iter.next());
			if (i == buttonId)
				return index;
			index++;
		}
		return -1;
	}

	protected boolean setNameByButtonId(int buttonId, String newName) {
		int index = getIndexByButtonId(buttonId);

		if (index != -1) {
			name.set(index, newName);
			return true;
		}
		return false;
	}

	protected boolean isEmtpy(int buttonId) {
		if (this.getIndexByButtonId(buttonId) == -1)
			return true;

		return false;
	}

	protected void printData() {
		Iterator<String> iter = buttonId.listIterator();
		System.out.println("+++ Contact Data Storage: +++");
		while (iter.hasNext()) {
			int buttonId = Integer.parseInt(iter.next());

			String lookup = getLookupKeyByButtonId(buttonId) == null ? ""
					: getLookupKeyByButtonId(buttonId);

			System.out.println("ButtonId:" + buttonId + "; CallerId:"
					+ getCallerIdByButtonId(buttonId) + "; Name:"
					+ getNameByButtonId(buttonId) + "; Number:"
					+ getNumberByButtonId(buttonId) + " LookupId:" + lookup);
		}
		System.out.println("length=" + buttonId.size()  + " / lookup=" + lookupKey.size());

		System.out.println("++++++++++++++++++++++++++++++++++++++++");
	}

	// /**
	// * Alternative Number storage using a Hashmap<buttonId, altNumberString>
	// *
	// * @param buttonId
	// */
	// protected void addAlternativeNumber(int buttonId, String number) {
	// this.alternativeNumber.put(buttonId, number);
	// }
	//
	// protected void removeAlternativeNumber(int buttonId) {
	// this.alternativeNumber.remove(buttonId);
	// }
	//
	// protected String getAlternativeNumber(int buttonId) {
	// return this.alternativeNumber.get(buttonId);
	// }
	//
	// protected boolean hasAlternativeNumber(int buttonId) {
	// return this.alternativeNumber.containsKey(buttonId);
	// }

	protected List<String> getButtonIdList() {
		return new ArrayList<String>(this.buttonId);
	}

	protected void setButtonIdList(List<String> bId) {
		this.buttonId = bId;
	}

	protected List<String> getcallerIdList() {
		return new ArrayList<String>(this.callerId);
	}

	protected void setcallerIdList(List<String> bId) {
		this.callerId = bId;
	}

	protected List<String> getnumberList() {
		return new ArrayList<String>(this.number);
	}

	protected void setnumberList(List<String> bId) {
		this.number = bId;
	}

	protected List<String> getnameList() {
		return new ArrayList<String>(this.name);
	}

	protected void setnameList(List<String> bId) {
		this.name = bId;
	}

	protected List<String> getInitializedLookupList() {
		lookupKey = new ArrayList<String>();
		int i = 0;
		
		while (i < this.buttonId.size()) {
			lookupKey.add((String)null);
			i++;
		}
		return lookupKey;
	}

}
