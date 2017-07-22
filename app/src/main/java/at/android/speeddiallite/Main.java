package at.android.speeddiallite;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.QuickContact;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Main extends Activity implements OnClickListener {

	protected static final int MAX_BUTTONS_COUNT1 = 12;
	protected static final int MAX_BUTTONS_COUNT2 = 0;
	protected static final int MAX_BUTTONS_COUNT3 = 0;
	protected static final int MAX_BUTTONS_COUNT4 = 0;

	protected static final int MAX_GROUPS_COUNT = 4;
	protected static final Boolean DEBUG_STARTUP = false;
	protected static final Boolean DEBUG_DATA_STORAGE = false;

	protected static final int BUTTONS_PER_ROW_S = 4;
	protected static final int BUTTONS_PER_ROW_LANDSCAPE_S = 6;
	protected static final int BUTTONS_PER_ROW_M = 3;
	protected static final int BUTTONS_PER_ROW_LANDSCAPE_M = 5;
	protected static final int BUTTONS_PER_ROW_L = 2;
	protected static final int BUTTONS_PER_ROW_LANDSCAPE_L = 4;

	private static final int CONTACT_PICKER_REQUEST_CODE = 1001;
	private static final String PREFS_NAME = "MyPrefsFile";

	private static final int SHORT_VIEW_CONTACT = 0;
	private static final int SHORT_CALL_CONTACT = 1;
	private static final int SHORT_SEND_MESSAGE = 2;
	private static final int SHORT_DIAL_CONTACT = 3;
	private static final int SHORT_COPY_TO_CLIP = 4;
	private static final int SHORT_CALL_MENU = 5;

	private static final int LONG_CONTEXT_MENU = 0;
	private static final int LONG_VIEW_CONTACT = 1;
	private static final int LONG_CALL_CONTACT = 2;
	private static final int LONG_SEND_MESSAGE = 3;
	private static final int LONG_DIAL_CONTACT = 4;
	private static final int LONG_COPY_TO_CLIP = 5;
	private static final int LONG_NONE = 6;
	private static final int LONG_CALL_MENU = 7;

	private Builder mNumberAlertDialog, mActionsAlertDialog,
			mRenameShortcutAlertDialog;
	private View mQuickContactView;
	private String contactId, name, hasPhoneNumber, number;
	private boolean mOnCreateCalled, mIsLiteVersion, mMoveInProgress = false,
			mIsICSorHigher = false, mActionInProgress = false,
			mViewContactInProgress = false, mSwipeInProgress = false,
			mQuickContactUsed = false;
	private int mLongClickedButtonId, mShortClickedButtonId,
			mRenameButtonId = -1, mButtonSizePx, mOrientation,
			mMoveFromButtonId = 0, mMoveToButtonId = 0, mButtonId = 0,
			mMaxShortcuts;

	private List<ImageView> mButtons1;
	private ImageView mButtonBack, mButtonForward, mButtonHome;
	private ViewFlipper mViewFlipper;
	private ContactDataStorage mContactDataStorage;
	private CharSequence[] mNumberItems, mActionsItems;
	private EditText mRenameInput;
	private Drawable mBackButtonDrawable, mHomeButtonDrawable,
			mForwardButtonDrawable, mBackButtonDrawableOver,
			mHomeButtonDrawableOver, mForwardButtonDrawableOver;

	// Global Preferences Variables
	private int mButtonsCount1, mButtonsCount2, mButtonsCount3, mButtonsCount4,
			mShortClickBehavior, mLongClickBehavior, mLayoutSize, mTextSize,
			mTextPosition, mBorderPx, mCornerRadiusPx, mDefaultImage;
	private boolean mHideStatusBar, mHideTitleBar, mAutoClose, mShowPlusIcon,
			mSaveLastGroupView, mShowNavBar, mShowNames, mActionbarVc,
			mActionbarLog, mActionbarCon, mActionbarDial, mActionbarAc,
			mActionbarSc, mActionbarSet;
	private String mTitleGroup1, mTitleGroup2, mTitleGroup3, mTitleGroup4;

	// Main Preferences Varialbes
	private int mLastGroupViewIndex;
	private String lookupKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (DEBUG_STARTUP)
			System.out.println("Main: onCreate(Bundle savedInstanceState)");

		mOnCreateCalled = true;
		mIsLiteVersion = this.getPackageName().toLowerCase().contains("lite");

		if (Build.VERSION.SDK_INT >= 14)
			mIsICSorHigher = true;

		loadSharedPreferences();
		mMaxShortcuts = getMaxShortcutsCount();
		updateViewMode();
		setContentBasedOnLayout();
		initializeComponents();
		loadDataStorage();
		updateButtonImages();
		hideButtons();

		// Save Data to File System. For Backup...
		// InternalFileStorage.getInstance().copyDataStorageToSD(this);

		if (DEBUG_DATA_STORAGE)
			mContactDataStorage.printData();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!mOnCreateCalled) {

			if (DEBUG_STARTUP)
				System.out.println("Main: onResume()");

			loadSharedPreferences();
			mMaxShortcuts = getMaxShortcutsCount();
			setContentBasedOnLayout();
			initializeComponents();
			updateButtonImages();
			hideButtons();

			if (mIsICSorHigher)// Needed to refresh the Action Bar
				ActivityCompat.invalidateOptionsMenu(this);
		}
		mOnCreateCalled = false;
		mQuickContactUsed = false;

		updateGroupTitle();
	}

	@Override
	protected void onPause() {
		super.onPause();

		saveSharedPreferences();

		if (Main.DEBUG_STARTUP)
			System.out.println("Main: onPause()");
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (Main.DEBUG_STARTUP)
			System.out.println("Main: onStop()");

		if (mAutoClose && mQuickContactUsed)
			this.finish();
	}

	private void setContentBasedOnLayout() {

		WindowManager winMan = (WindowManager) getBaseContext()
				.getSystemService(Context.WINDOW_SERVICE);

		if (winMan != null) {

			mOrientation = winMan.getDefaultDisplay().getOrientation();

			if (mOrientation == 0) {
				switch (mLayoutSize) {
				case 0:
					setContentView(R.layout.main_s0);
					mButtonSizePx = getResources().getDisplayMetrics().widthPixels
							/ BUTTONS_PER_ROW_S;
					break;
				case 1:
					if (mMaxShortcuts == MAX_BUTTONS_COUNT1)
						setContentView(R.layout.main_m0_30);
					else if (mMaxShortcuts == MAX_BUTTONS_COUNT1
							+ MAX_BUTTONS_COUNT2)
						setContentView(R.layout.main_m0_60);
					else if (mMaxShortcuts == MAX_BUTTONS_COUNT1
							+ MAX_BUTTONS_COUNT2 + MAX_BUTTONS_COUNT3)
						setContentView(R.layout.main_m0_90);
					else
						setContentView(R.layout.main_m0);
					mButtonSizePx = getResources().getDisplayMetrics().widthPixels
							/ BUTTONS_PER_ROW_M;
					break;
				case 2:
					setContentView(R.layout.main_l0);
					mButtonSizePx = getResources().getDisplayMetrics().widthPixels
							/ BUTTONS_PER_ROW_L;
					break;

				default:
					break;
				}

			} else {
				switch (mLayoutSize) {
				case 0:
					setContentView(R.layout.main_s0_land);
					mButtonSizePx = getResources().getDisplayMetrics().widthPixels
							/ BUTTONS_PER_ROW_LANDSCAPE_S;
					break;
				case 1:
					if (mMaxShortcuts == MAX_BUTTONS_COUNT1)
						setContentView(R.layout.main_m0_land_30);
					else if (mMaxShortcuts == MAX_BUTTONS_COUNT1
							+ MAX_BUTTONS_COUNT2)
						setContentView(R.layout.main_m0_land_60);
					else if (mMaxShortcuts == MAX_BUTTONS_COUNT1
							+ MAX_BUTTONS_COUNT2 + MAX_BUTTONS_COUNT3)
						setContentView(R.layout.main_m0_land_90);
					else
						setContentView(R.layout.main_m0_land);
					mButtonSizePx = getResources().getDisplayMetrics().widthPixels
							/ BUTTONS_PER_ROW_LANDSCAPE_M;
					break;
				case 2:
					setContentView(R.layout.main_l0_land);
					mButtonSizePx = getResources().getDisplayMetrics().widthPixels
							/ BUTTONS_PER_ROW_LANDSCAPE_L;
					break;

				default:
					break;
				}
			}
		}
	}

	private void updateViewMode() {

		if (mHideStatusBar) {

			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

		}
		if (mHideTitleBar) {

			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
	}

	private void loadSharedPreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);

		// Global Preferences Variables
		mHideStatusBar = settings.getBoolean("mHideStatusBar", false);
		mHideTitleBar = settings.getBoolean("mHideTitleBar", false);
		mShowPlusIcon = settings.getBoolean("mShowPlusIcon", true);
		mSaveLastGroupView = settings.getBoolean("mSaveLastGroupView", true);
		mShowNavBar = settings.getBoolean("mShowNavBar", true);
		mShowNames = settings.getBoolean("mShowNames", false);
		mButtonsCount1 = settings.getInt("mButtonsCount1v2",
				Main.MAX_BUTTONS_COUNT1);
		mButtonsCount2 = settings.getInt("mButtonsCount2v2",
				Main.MAX_BUTTONS_COUNT2);
		mButtonsCount3 = settings.getInt("mButtonsCount3v2",
				Main.MAX_BUTTONS_COUNT3);
		mButtonsCount4 = settings.getInt("mButtonsCount4v2",
				Main.MAX_BUTTONS_COUNT4);
		mShortClickBehavior = settings.getInt("mShortClickBehavior", 1);
		mLongClickBehavior = settings.getInt("mLongClickBehavior", 0);
		mLayoutSize = settings.getInt("mLayoutSize", 1);
		mTextPosition = settings.getInt("mTextPosition", 2);
		mTextSize = settings.getInt("mTextSizeV2", 4);
		mBorderPx = settings.getInt("mBorderPx", 0);
		mCornerRadiusPx = settings.getInt("mCornerRadiusPx", 0);
		mDefaultImage = settings.getInt("mDefaultImage", 0);
		mAutoClose = settings.getBoolean("mAutoClose", true);
		mTitleGroup1 = settings.getString("mTitleGroup1", getResources()
				.getString(R.string.app_name));
		mTitleGroup2 = settings.getString("mTitleGroup2", getResources()
				.getString(R.string.app_name));
		mTitleGroup3 = settings.getString("mTitleGroup3", getResources()
				.getString(R.string.app_name));
		mTitleGroup4 = settings.getString("mTitleGroup4", getResources()
				.getString(R.string.app_name));
		mActionbarVc = settings.getBoolean("mActionbarVc", false);
		mActionbarLog = settings.getBoolean("mActionbarLog", false);
		mActionbarCon = settings.getBoolean("mActionbarCon", false);
		mActionbarDial = settings.getBoolean("mActionbarDial", false);
		mActionbarAc = settings.getBoolean("mActionbarAc", false);
		mActionbarSc = settings.getBoolean("mActionbarSc", false);
		mActionbarSet = settings.getBoolean("mActionbarSet", false);

		// Main Preferences Varialbes
		mLastGroupViewIndex = settings.getInt("mLastGroupViewIndex", 0);

	}

	private void saveSharedPreferences() {

		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		// Global Preferences Variables
		// none

		// Main Preferences Varialbes
		editor.putInt("mLastGroupViewIndex", mViewFlipper.getDisplayedChild());

		editor.commit();

	}

	// /**
	// * Defines whether an Actionbar Item is shown.
	// */
	// private void setActionbarItemVisibility(Method methodSetShowAsAction,
	// Menu menu) {
	// try {
	// int showFlag = 2; // 0=never, 1=ifroom, 2=always;
	//
	// if (mActionbarVc)
	// methodSetShowAsAction.invoke(
	// (MenuItem) menu.findItem(R.id.menu_viewcontact),
	// showFlag);
	// if (mActionbarLog)
	// methodSetShowAsAction.invoke(
	// (MenuItem) menu.findItem(R.id.menu_calllog), showFlag);
	// if (mActionbarCon)
	// methodSetShowAsAction.invoke(
	// (MenuItem) menu.findItem(R.id.menu_contacts), showFlag);
	// if (mActionbarDial)
	// methodSetShowAsAction.invoke(
	// (MenuItem) menu.findItem(R.id.menu_dialer), showFlag);
	// if (mActionbarAc)
	// methodSetShowAsAction.invoke(
	// (MenuItem) menu.findItem(R.id.menu_actions), showFlag);
	// if (mActionbarSc)
	// methodSetShowAsAction.invoke(
	// (MenuItem) menu.findItem(R.id.menu_scan), showFlag);
	// if (mActionbarSet)
	// methodSetShowAsAction.invoke(
	// (MenuItem) menu.findItem(R.id.menu_settings), showFlag);
	//
	// // m1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * Either Inflate the Main Menu for ICS or the Gingerbread menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();

		if (mIsICSorHigher) {
			inflater.inflate(R.menu.main_menu_actionbar, menu);

			int showFlag = MenuItemCompat.SHOW_AS_ACTION_ALWAYS;

			if (mActionbarVc)
				MenuItemCompat.setShowAsAction(
						(MenuItem) menu.findItem(R.id.menu_viewcontact),
						showFlag);
			if (mActionbarLog)
				MenuItemCompat.setShowAsAction(
						(MenuItem) menu.findItem(R.id.menu_calllog), showFlag);
			if (mActionbarCon)
				MenuItemCompat.setShowAsAction(
						(MenuItem) menu.findItem(R.id.menu_contacts), showFlag);
			if (mActionbarDial)
				MenuItemCompat.setShowAsAction(
						(MenuItem) menu.findItem(R.id.menu_dialer), showFlag);
			if (mActionbarAc)
				MenuItemCompat.setShowAsAction(
						(MenuItem) menu.findItem(R.id.menu_actions), showFlag);
			if (mActionbarSc)
				MenuItemCompat.setShowAsAction(
						(MenuItem) menu.findItem(R.id.menu_scan), showFlag);
			if (mActionbarSet)
				MenuItemCompat.setShowAsAction(
						(MenuItem) menu.findItem(R.id.menu_settings), showFlag);

			// Method methodSetShowAsAction = null;
			// methodSetShowAsAction = MenuItem.class.getMethod(
			// "setShowAsAction", new Class[] { int.class });
			// setActionbarItemVisibility(methodSetShowAsAction, menu);

		} else
			inflater.inflate(R.menu.main_menu, menu);

		return true;
	}

	/**
	 * Callback method from Main Menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_settings:
			Intent intent = new Intent(this, Settings.class);
			startActivity(intent);
			// startActivityForResult(intent, SETTING_REQUEST_CODE);
			return true;
		case R.id.menu_scan:
			rescanMedia();
			return true;
		case R.id.menu_calllog:
			showCallLog();
			return true;
		case R.id.menu_actions:
			startActions();
			return true;
		case R.id.menu_viewcontact:
			startViewContact();
			return true;
		case R.id.menu_dialer:
			showKeypad();
			return true;
		case R.id.menu_contacts:
			showContacts();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Edit a shortcut using the main menu
	 */
	private void startActions() {
		mActionInProgress = true;
		Toast.makeText(this, R.string.toast_editShortcut, Toast.LENGTH_SHORT)
				.show();
	}

	private void startViewContact() {
		mViewContactInProgress = true;
		Toast.makeText(this, R.string.toast_editShortcut, Toast.LENGTH_SHORT)
				.show();

	}

	/**
	 * open the Call Log
	 */
	private void showCallLog() {
		Intent showCallLog = new Intent();
		showCallLog.setAction(Intent.ACTION_VIEW);
		showCallLog.setType(CallLog.Calls.CONTENT_TYPE);
		startActivity(showCallLog);

		if (mAutoClose)
			this.finish();
	}

	private void showKeypad() {
		Intent showKeypad = new Intent(Intent.ACTION_DIAL);
		startActivity(showKeypad);

		if (mAutoClose)
			this.finish();
	}

	private void showContacts() {
		Intent showContacts = new Intent(Intent.ACTION_VIEW,
				ContactsContract.Contacts.CONTENT_URI);
		startActivity(showContacts);

		// if (mAutoClose)
		// this.finish();
	}

	/**
	 * Long Click performed
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		if (mMoveInProgress || mActionInProgress || mViewContactInProgress) {
			mMoveInProgress = false;
			mActionInProgress = false;
			mViewContactInProgress = false;
			mMoveFromButtonId = 0;
			mMoveToButtonId = 0;
			Toast.makeText(Main.this, R.string.toast_cancelled,
					Toast.LENGTH_SHORT).show();
			return;
		}

		mLongClickedButtonId = Integer.parseInt((String) v.getTag());

		Boolean isEmptyShortcut = mContactDataStorage
				.isEmtpy(mLongClickedButtonId);

		if (isEmptyShortcut)
			startContactPickerActivity(mLongClickedButtonId);

		else {

			switch (mLongClickBehavior) {
			case LONG_CONTEXT_MENU:
				mQuickContactView = v;
				super.onCreateContextMenu(menu, v, menuInfo);
				menu.setHeaderTitle(mContactDataStorage
						.getNameByButtonId(mLongClickedButtonId));

				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.main_contextmenu, menu);

				break;
			case LONG_VIEW_CONTACT:
				viewContact(mLongClickedButtonId);
				break;
			case LONG_CALL_CONTACT:
				callContactByButtonId(mLongClickedButtonId, true);
				break;
			case LONG_SEND_MESSAGE:
				sendMessageByButtonId(mLongClickedButtonId);
				break;
			case LONG_DIAL_CONTACT:
				callContactByButtonId(mLongClickedButtonId, false);
				break;
			case LONG_COPY_TO_CLIP:
				copyNumberToClipboardByButtonId(mLongClickedButtonId);
				break;
			case LONG_NONE:
				break;
			case LONG_CALL_MENU:
				showQuickContactMenu(mLongClickedButtonId, v);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Callback method from Context Menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.contextmenu_viewContact:
			viewContact(mLongClickedButtonId);
			return true;
		case R.id.contextmenu_sendmsg:
			sendMessageByButtonId(mLongClickedButtonId);
			return true;
		case R.id.contextmenu_call:
			callContactByButtonId(mLongClickedButtonId, true);
			return true;
		case R.id.contextmenu_callmenu:
			showQuickContactMenu(mLongClickedButtonId, mQuickContactView);
			return true;
		case R.id.contextmenu_replace:
			return startContactPickerActivity(mLongClickedButtonId);
		case R.id.contextmenu_remove:
			removeShortcut(mLongClickedButtonId);
			return true;
		case R.id.contextmenu_move:
			moveShortcut(mLongClickedButtonId);
			return true;
		case R.id.contextmenu_rename:
			renameShortcutByButtonId(mLongClickedButtonId);
			return true;
		case R.id.contextmenu_dial:
			callContactByButtonId(mLongClickedButtonId, false);
			return true;
		case R.id.contextmenu_copy:
			copyNumberToClipboardByButtonId(mLongClickedButtonId);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void moveShortcut(int buttonId) {

		mMoveInProgress = true;
		mMoveFromButtonId = buttonId;

		Toast.makeText(this, R.string.toast_moveShortcut, Toast.LENGTH_SHORT)
				.show();

	}

	private void removeShortcut(int buttonId) {

		mContactDataStorage.removeDataRowByButtonId(buttonId);
		updateButtonImages();
		saveDataStorage();
		Toast.makeText(this, R.string.toast_removed, Toast.LENGTH_SHORT).show();
	}

	private void renameShortcutByButtonId(int buttonId) {
		mRenameButtonId = buttonId;

		mRenameInput = new EditText(this);
		mRenameInput.setText(mContactDataStorage.getNameByButtonId(buttonId));
		mRenameInput.setLines(1);
		mRenameInput.setSingleLine(true);

		mRenameShortcutAlertDialog = new AlertDialog.Builder(this);
		mRenameShortcutAlertDialog.setMessage(getResources().getString(
				R.string.alertdialog_maxWords));
		mRenameShortcutAlertDialog.setView(mRenameInput);
		mRenameShortcutAlertDialog.setTitle(getResources().getString(
				R.string.contextmenu_rename));
		mRenameShortcutAlertDialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// System.out.println(mRenameInput.getText().toString()
						// + " ButtonID: " + mRenameButtonId);

						if (mContactDataStorage.setNameByButtonId(
								mRenameButtonId, mRenameInput.getText()
										.toString())) {

							updateButtonImages();
							saveDataStorage();
							Toast.makeText(getBaseContext(),
									R.string.toast_saved, Toast.LENGTH_SHORT)
									.show();
						} else {
							Toast.makeText(getBaseContext(),
									R.string.toast_notSaved, Toast.LENGTH_SHORT)
									.show();
						}
					}
				});
		mRenameShortcutAlertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		mRenameShortcutAlertDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						Toast.makeText(Main.this, R.string.toast_cancelled,
								Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				});
		mRenameShortcutAlertDialog.show();
	}

	/**
	 * Either the default short click is executed depending on the selected
	 * default behavior or the 'move shortcut' of 'actions' operation is
	 * executed. A new row will be added with the old shortcut data and
	 * afterwards the old shortcut will be deleted. Or the actions menu pops up
	 */
	@Override
	public void onClick(View v) {

		mShortClickedButtonId = Integer.parseInt((String) v.getTag());
		// mShortClickedButtonResourceId = v.getId();

		if (mMoveInProgress) {

			mMoveInProgress = false;
			mMoveToButtonId = mShortClickedButtonId;

			if (mMoveFromButtonId != mMoveToButtonId) {
				// add new shortcut with data from old one
				mContactDataStorage.addDataRow(String.valueOf(mMoveToButtonId),
						mContactDataStorage
								.getCallerIdByButtonId(mMoveFromButtonId),
						mContactDataStorage
								.getNumberByButtonId(mMoveFromButtonId),
						mContactDataStorage
								.getNameByButtonId(mMoveFromButtonId),
						mContactDataStorage
								.getLookupKeyByButtonId(mMoveFromButtonId));

				// remove old shortcut
				mContactDataStorage.removeDataRowByButtonId(mMoveFromButtonId);

				updateButtonImages();
				saveDataStorage();

				Toast.makeText(Main.this, R.string.toast_moved,
						Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(Main.this, R.string.toast_cancelled,
						Toast.LENGTH_SHORT).show();
			}

			mMoveFromButtonId = 0;
			mMoveToButtonId = 0;

		} else if (mActionInProgress) {

			mActionInProgress = false;
			showActionsAlertDialog(v);

		} else if (mViewContactInProgress) {
			mViewContactInProgress = false;
			viewContact(mShortClickedButtonId);

		} else {
			switch (mShortClickBehavior) {
			case SHORT_VIEW_CONTACT:
				viewContact(mShortClickedButtonId);
				break;
			case SHORT_CALL_CONTACT:
				// call Contact using clicked Button Tag
				callContactByButtonId(mShortClickedButtonId, true);
				break;
			case SHORT_SEND_MESSAGE:
				sendMessageByButtonId(mShortClickedButtonId);
				break;
			case SHORT_DIAL_CONTACT:
				// dial Contact using clicked Button Tag
				callContactByButtonId(mShortClickedButtonId, false);
				break;
			case SHORT_COPY_TO_CLIP:
				copyNumberToClipboardByButtonId(mShortClickedButtonId);
				break;
			case SHORT_CALL_MENU:
				showQuickContactMenu(mShortClickedButtonId, v);
				break;
			default:
				break;
			}

		}

		if (DEBUG_DATA_STORAGE)
			mContactDataStorage.printData();
	}

	/**
	 * Send Contact Picker Intent
	 * 
	 * @return
	 */
	private boolean startContactPickerActivity(int buttonId) {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.ContactsContract.Contacts.CONTENT_URI);

		mButtonId = buttonId;

		startActivityForResult(intent, CONTACT_PICKER_REQUEST_CODE);
		return true;
	}

	/**
	 * Retrieve id, name and hasNumber from contact picker that returns an URI,
	 * if phone number exists, read number from another table. If more than one
	 * number is available, display a popup list alert dialog to choose a
	 * number. Afterwards add data to mContactDataStorage or break if no number
	 * is selected
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_REQUEST_CODE:

				Uri contactData = data.getData();
				Cursor contactsCursor = managedQuery(contactData, null, null,
						null, null);

				if (contactsCursor.moveToFirst()) {

					contactId = contactsCursor.getString(contactsCursor
							.getColumnIndexOrThrow(BaseColumns._ID));
					lookupKey = contactsCursor
							.getString(contactsCursor
									.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY));
					name = contactsCursor
							.getString(contactsCursor
									.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
					hasPhoneNumber = contactsCursor
							.getString(contactsCursor
									.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));

					if (Integer.parseInt(hasPhoneNumber) > 0) {
						Cursor numberCursor = getContentResolver()
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = ?",
										new String[] { contactId }, null);

						// Contact contains only one Number
						if (numberCursor.getCount() == 1) {

							while (numberCursor.moveToNext()) {
								number = numberCursor
										.getString(numberCursor
												.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
							}

							mContactDataStorage.addDataRow(
									Integer.toString(mButtonId), contactId,
									number, name, lookupKey);

							updateButtonImages();
							saveDataStorage();

							Toast.makeText(this, R.string.toast_saved,
									Toast.LENGTH_SHORT).show();

						}
						// Contact contains more than one number
						else if (numberCursor.getCount() > 1) {

							mNumberItems = new String[numberCursor.getCount()];

							while (numberCursor.moveToNext()) {
								mNumberItems[numberCursor.getPosition()] = new String(
										numberCursor.getString(numberCursor
												.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
							}

							showPickANumberAlertDialog();

						}

						// No Number stored in Contact
					} else {
						Toast.makeText(this, R.string.toast_noNumber,
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			}
		}
	}

	/**
	 * Build URI containing phone number and start Intent
	 * 
	 * @param buttonId
	 */
	private void callContactByButtonId(int buttonId, boolean instantCall) {

		String number = mContactDataStorage.getNumberByButtonId(buttonId);

		if (number != null) {

			// used for ussd codes (replace # with %23)
			number = Uri.encode(number);

			String uri = "tel:" + number;

			Intent intent;
			if (instantCall)
				intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
			else
				intent = new Intent(Intent.ACTION_DIAL, Uri.parse(uri));

			startActivity(intent);

			if (mAutoClose)
				this.finish();
		}
	}

	private void viewContact(int buttonId) {

		String contactId = mContactDataStorage.getCallerIdByButtonId(buttonId);

		String lookupKey;
		Uri contactUri;

		// Here we do backward compatibility stuff
		try {
			lookupKey = mContactDataStorage.getLookupKeyByButtonId(buttonId);
		} catch (Exception e) {
			// in this case we need to convert the data object
			lookupKey = null;
		}

		if (lookupKey != null) {
			Uri lookupUri = Uri.withAppendedPath(
					ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
			contactUri = ContactsContract.Contacts.lookupContact(
					getContentResolver(), lookupUri);
		} else {
			// this is just for backwards compatibility but cannot work because
			// the contact Id can change on each contact sync
			contactUri = Uri.withAppendedPath(
					ContactsContract.Contacts.CONTENT_URI, "" + contactId);
		}

		if (contactId != null) {

			Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);

			startActivity(intent);

			if (mAutoClose)
				this.finish();
		}

	}

	/**
	 * Build URI containing phone number and start Intent
	 * 
	 * @param buttonId
	 */
	private void sendMessageByButtonId(int buttonId) {

		String number = mContactDataStorage.getNumberByButtonId(buttonId);

		if (number != null) {

			String uri = "sms:" + number;
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(uri));
			// intent.putExtra("sms_body", x);

			startActivity(intent);

			if (mAutoClose)
				this.finish();
		}
	}

	/**
	 * If number exists, copy to Clipboard and send Toast
	 * 
	 * @param buttonId
	 */
	private void copyNumberToClipboardByButtonId(int buttonId) {

		String number = mContactDataStorage.getNumberByButtonId(buttonId);

		if (number != null) {

			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

			clipboard.setText(number);

			Toast.makeText(this, R.string.toast_copyToClip, Toast.LENGTH_SHORT)
					.show();

			if (mAutoClose)
				this.finish();
		}
	}

	private void showQuickContactMenu(int buttonId, View v) {

		String lookupKey;
		Uri contactUri = null;

		try {
			String contactId = mContactDataStorage
					.getCallerIdByButtonId(buttonId);

			// Here we do backward compatibility stuff
			try {
				lookupKey = mContactDataStorage
						.getLookupKeyByButtonId(buttonId);
			} catch (Exception e) {
				// in this case we need to convert the data object
				lookupKey = null;
			}

			if (lookupKey != null) {
				Uri lookupUri = Uri
						.withAppendedPath(
								ContactsContract.Contacts.CONTENT_LOOKUP_URI,
								lookupKey);
				contactUri = ContactsContract.Contacts.lookupContact(
						getContentResolver(), lookupUri);
			} else {
				// this is just for backwards compatibility but cannot work
				// becaus the contact Id can change on each contact sync
				contactUri = Uri.withAppendedPath(
						ContactsContract.Contacts.CONTENT_URI, "" + contactId);
			}

			mQuickContactUsed = true;

			if (contactUri != null)
				QuickContact.showQuickContact(getApplicationContext(), v,
						contactUri, QuickContact.MODE_SMALL, null);
			else
				Toast.makeText(getApplicationContext(),
						"Ups, your device does not support this feature!",
						Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Ups, your device does not support this feature!",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void rescanMedia() {
		sendBroadcast(new Intent(
				Intent.ACTION_MEDIA_MOUNTED,
				Uri.parse("file://" + Environment.getExternalStorageDirectory())));

		Toast.makeText(this, R.string.toast_mediaScanner, Toast.LENGTH_SHORT)
				.show();

		if (mAutoClose)
			this.finish();
	}

	/**
	 * Builds an URI to retrieve the contact photo using the contact ID
	 * 
	 * @param contentResolver
	 * @param contactId
	 * @return
	 */
	private Bitmap getContactPhotoById(ContentResolver contentResolver,
			Long contactId, String lookupKey) {

		Uri contactPhotoUri;

		if (lookupKey != null) {
			Uri lookupUri = Uri.withAppendedPath(
					ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
			contactPhotoUri = ContactsContract.Contacts.lookupContact(
					getContentResolver(), lookupUri);
		} else {
			// this is just for backwards compatibility but cannot work becuase
			// the contact Id can change on each contact sync
			contactPhotoUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
					contactId);
		}

		InputStream photoDataStream = null;

		if (mIsICSorHigher) { // Retrieve High Resolution photo 256x256
			Method methodOpenContactPhotoInputStream = null;
			try {
				methodOpenContactPhotoInputStream = ContactsContract.Contacts.class
						.getMethod("openContactPhotoInputStream",
								new Class[] { ContentResolver.class, Uri.class,
										boolean.class });
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			try {
				photoDataStream = (InputStream) methodOpenContactPhotoInputStream
						.invoke(null, contentResolver, contactPhotoUri, true);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

		} else { // Retrieve Low Resolution thumbnail 96x96
			try {
				photoDataStream = Contacts.openContactPhotoInputStream(
						contentResolver, contactPhotoUri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Bitmap photo;

		if (photoDataStream != null) {
			photo = BitmapFactory.decodeStream(photoDataStream);
			try {
				photoDataStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			return null;
		}

		return photo;
	}

	/**
	 * load mContactDataStorage object and pass this (Activity/Context) as
	 * parameter
	 */
	private void loadDataStorage() {
		InternalFileStorage wotf = InternalFileStorage.getInstance();
		mContactDataStorage = wotf.loadObjectFromFile(this);
	}

	/**
	 * save mContactDataStorage object and pass this (Activity/Context) and the
	 * data object as parameter
	 */
	private void saveDataStorage() {
		InternalFileStorage wotf = InternalFileStorage.getInstance();
		wotf.saveObjectToFile(mContactDataStorage, this);
	}

	// private void showInfoAlertDialog() {
	//
	// String versionName = null;
	//
	// try {
	// versionName = getPackageManager().getPackageInfo(getPackageName(),
	// 0).versionName;
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// mInfoAlertDialog = new AlertDialog.Builder(this).create();
	// mInfoAlertDialog.setTitle("Info");
	//
	// String message = getResources().getString(R.string.app_name) + "\n\n"
	// + "Version: " + versionName + "\n\n"
	// + getResources().getString(R.string.dev_name) + ".";
	//
	// message += "\n\n"
	// + getResources().getString(R.string.alertdialog_info_help);
	//
	// if (DEBUG_STARTUP) {
	// float scaleDensity = getResources().getDisplayMetrics().density;
	//
	// String resolution = String.valueOf(getResources()
	// .getDisplayMetrics().widthPixels)
	// + "x"
	// + String.valueOf(getResources().getDisplayMetrics().heightPixels);
	//
	// String density = String.valueOf(Math.round(getResources()
	// .getDisplayMetrics().xdpi))
	// + "x"
	// + String.valueOf(Math.round(getResources()
	// .getDisplayMetrics().ydpi));
	//
	// message += "\n\nResolution: " + resolution + "\n" + "Density: "
	// + density + "\n" + "Density Scale: " + scaleDensity + "\n"
	// + "Orientation: " + mOrientation;
	// }
	//
	// mInfoAlertDialog.setMessage(message);
	//
	// mInfoAlertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// mInfoAlertDialog.dismiss();
	// }
	// });
	// mInfoAlertDialog.show();
	// }

	/**
	 * shows a List Alert dialog with all available numbers of one contact.
	 * 'mNumberItems' is a global field created in onActivityResult(int, int,
	 * Intent). Sends Toast whether operation was successful
	 */
	private void showPickANumberAlertDialog() {

		mNumberAlertDialog = new AlertDialog.Builder(this);
		mNumberAlertDialog.setTitle(R.string.alertdialog_pickNumberTitle);
		mNumberAlertDialog.setItems(mNumberItems,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						number = mNumberItems[which].toString();

						mContactDataStorage.addDataRow(
								Integer.toString(mButtonId), contactId, number,
								name, lookupKey);

						updateButtonImages();
						saveDataStorage();

						Toast.makeText(Main.this, R.string.toast_saved,
								Toast.LENGTH_SHORT).show();

						if (DEBUG_DATA_STORAGE)
							mContactDataStorage.printData();

						// showAlternativeNumberAlertDialog();

						return;
					}
				});
		mNumberAlertDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						Toast.makeText(Main.this, R.string.toast_notSaved,
								Toast.LENGTH_SHORT).show();

					}
				});

		mNumberAlertDialog.create().show();

	}

	private void showActionsAlertDialog(View v) {

		mQuickContactView = v;

		if (mContactDataStorage.isEmtpy(mShortClickedButtonId)) {
			mActionsItems = new String[1];
			mActionsItems[0] = getResources().getString(
					R.string.contextmenu_add);

		} else {

			mActionsItems = new String[10];
			mActionsItems[0] = getResources().getString(
					R.string.contextmenu_viewContact);
			mActionsItems[1] = getResources().getString(
					R.string.contextmenu_call);
			mActionsItems[2] = getResources().getString(
					R.string.contextmenu_sendmsg);
			mActionsItems[3] = getResources().getString(
					R.string.contextmenu_callmenu);
			mActionsItems[4] = getResources().getString(
					R.string.contextmenu_replace);
			mActionsItems[5] = getResources().getString(
					R.string.contextmenu_remove);
			mActionsItems[6] = getResources().getString(
					R.string.contextmenu_move);
			mActionsItems[7] = getResources().getString(
					R.string.contextmenu_rename);
			mActionsItems[8] = getResources().getString(
					R.string.contextmenu_dial);
			mActionsItems[9] = getResources().getString(
					R.string.contextmenu_copy);
		}

		mActionsAlertDialog = new AlertDialog.Builder(this);
		mActionsAlertDialog.setTitle(R.string.alertdialog_actionsTitle);
		mActionsAlertDialog.setItems(mActionsItems,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							if (mContactDataStorage
									.isEmtpy(mShortClickedButtonId))
								startContactPickerActivity(mShortClickedButtonId);
							else
								viewContact(mShortClickedButtonId);
							break;
						case 1:
							callContactByButtonId(mShortClickedButtonId, true);
							break;
						case 2:
							sendMessageByButtonId(mShortClickedButtonId);
							break;
						case 3:
							showQuickContactMenu(mShortClickedButtonId,
									mQuickContactView);
							break;
						case 4:
							startContactPickerActivity(mShortClickedButtonId);
							break;
						case 5:
							removeShortcut(mShortClickedButtonId);
							break;
						case 6:
							moveShortcut(mShortClickedButtonId);
							break;
						case 7:
							renameShortcutByButtonId(mShortClickedButtonId);
							break;
						case 8:
							callContactByButtonId(mShortClickedButtonId, false);
							break;
						case 9:
							copyNumberToClipboardByButtonId(mShortClickedButtonId);
							break;
						default:
							break;
						}
					}
				});

		mActionsAlertDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						Toast.makeText(Main.this, R.string.toast_cancelled,
								Toast.LENGTH_SHORT).show();
					}
				});
		mActionsAlertDialog.create().show();

	}

	public Bitmap getDefaultImage(int selection) {

		int resId = getResources().getIdentifier("main_contact" + selection,
				"drawable", this.getPackageName());

		if (resId != 0)
			return BitmapFactory.decodeResource(getResources(), resId);

		return BitmapFactory.decodeResource(getResources(),
				R.drawable.main_contact);
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float radius) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		float roundPx = radius;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	/**
	 * iterate over all button/image views and set image if existing, if no
	 * contact stored show "plus" icon, if no caller image available show name
	 * of contact. mButtonSizePx is a global variable calculated depending on
	 * the resolution of the phone. updating all existing images in the
	 * ListArray. Caller Image Bitmaps will be resized depending on
	 * mButtonSizePx.
	 */
	private void updateButtonImages() {
		String callerId;
		String lookupKey;
		int index = 1;

		for (ImageView button : mButtons1) {

			if (index > mMaxShortcuts)
				break;

			callerId = mContactDataStorage.getCallerIdByButtonId(index);

			// Here we do backward compatibility stuff
			try {
				lookupKey = mContactDataStorage.getLookupKeyByButtonId(index);
			} catch (Exception e) {
				// in this case we need to convert the data object
				lookupKey = null;
			}

			if (callerId != null) {
				Bitmap bitmap = getContactPhotoById(getContentResolver(),
						Long.parseLong(callerId), lookupKey);

				if (bitmap != null) {
					// Contact has Image (Resize image and set to Image View)

					bitmap = resizeImage(bitmap, mButtonSizePx
							- (2 * mBorderPx), mButtonSizePx - (2 * mBorderPx));

					if (mShowNames) {
						if (!bitmap.isMutable())
							bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
						addTextToBitmap(bitmap,
								mContactDataStorage.getNameByButtonId(index));
					}

					if (mCornerRadiusPx > 0)
						button.setImageBitmap(getRoundedCornerBitmap(bitmap,
								mCornerRadiusPx));
					else
						button.setImageBitmap(bitmap);

				} else {
					// No Caller Image

					Bitmap defaultConatact = getDefaultImage(mDefaultImage);

					// make bitmap mutable
					if (!defaultConatact.isMutable())
						defaultConatact = defaultConatact.copy(
								Bitmap.Config.ARGB_8888, true);

					// native size of default caller image is 160px
					defaultConatact = resizeImage(defaultConatact,
							mButtonSizePx - (2 * mBorderPx), mButtonSizePx
									- (2 * mBorderPx));
					// System.out.println("resizing default caller image");

					addTextToBitmap(defaultConatact,
							mContactDataStorage.getNameByButtonId(index));

					if (mCornerRadiusPx > 0)
						button.setImageBitmap(getRoundedCornerBitmap(
								defaultConatact, mCornerRadiusPx));
					else
						button.setImageBitmap(defaultConatact);

				}

			} else {
				// Button with no Shortcut
				if (mShowPlusIcon) {
					button.setImageResource(R.drawable.main_add_ics);
				} else
					button.setImageDrawable(null);
			}

			index++;
		}
	}

	private Bitmap resizeImage(Bitmap bitmap, int buttonSizePxX,
			int buttonSizePxY) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scaleWidth = ((float) buttonSizePxX) / width;
		float scaleHeight = ((float) buttonSizePxY) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

	}

	private void addTextToBitmap(Bitmap bitmap, String text) {

		List<String> finalText = new ArrayList<String>();

		if (bitmap.isMutable()) {
			// Canvas must have a mutable bitmap to draw onto it
			Canvas canvas = new Canvas(bitmap);

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			float fontSize = (mTextSize + 10) * metrics.scaledDensity;

			int gap = 2;
			float xOffset = mButtonSizePx / 2;
			float yOffset = 0;

			switch (mTextPosition) {
			case 0: // TOP
				// yOffset = fontSize + gap + (2 * mBorderPx);
				yOffset = fontSize + mBorderPx;
				break;
			case 1: // CENTER
				yOffset = (mButtonSizePx / 2) - mBorderPx;
				break;
			case 2: // BOTTOM
				yOffset = mButtonSizePx - (fontSize + 4 * gap)
						- (3 * mBorderPx);
				break;

			default:
				break;
			}

			text = text.replace(",", "");
			CharSequence[] csa = text.split(" ");
			int textLines = 0;

			for (CharSequence cs : csa) {
				if (cs.length() > 0) {
					finalText.add(cs.toString());
					textLines++;
				}
				if (textLines == 2)
					break;
			}

			// paint.setStyle(Paint.Style.STROKE);
			// paint.setStrokeJoin(Paint.Join.ROUND);
			// paint.setStrokeCap(Paint.Cap.ROUND);
			// paint.setStrokeWidth(2);

			Paint paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.FILL);
			paint.setTextSize(fontSize);
			paint.setTextAlign(Align.CENTER);
			paint.setAntiAlias(true);
			paint.setTypeface(Typeface.DEFAULT_BOLD);
			paint.setShadowLayer(2, 2, 2, Color.BLACK);

			String replacedText;

			switch (finalText.size()) {
			case 1:
				replacedText = finalText.get(0).replace("_", " ");
				canvas.drawText(replacedText, xOffset, yOffset + (fontSize / 2)
						- gap, paint);
				break;
			case 2:
				replacedText = finalText.get(0).replace("_", " ");
				canvas.drawText(replacedText, xOffset, yOffset - (gap / 2),
						paint);

				replacedText = finalText.get(1).replace("_", " ");
				canvas.drawText(replacedText, xOffset, yOffset + (gap / 2)
						+ (fontSize), paint);
				break;
			default:
				break;
			}
		}
	}

	private void initializeComponents() {

		mButtons1 = new ArrayList<ImageView>();

		int allButtonsCount = MAX_BUTTONS_COUNT1 + MAX_BUTTONS_COUNT2
				+ MAX_BUTTONS_COUNT3 + MAX_BUTTONS_COUNT4;

		for (int i = 1; i <= allButtonsCount; i++) {

			if (i > mMaxShortcuts)
				break;

			int buttonResId = getResources().getIdentifier("ImageView" + i,
					"id", this.getPackageName());

			ImageView button = (ImageView) findViewById(buttonResId);

			button.setOnClickListener(this);
			registerForContextMenu(button);

			button.setMinimumWidth(mButtonSizePx);
			button.setMinimumHeight(mButtonSizePx);
			button.setScaleType(ScaleType.CENTER);

			mButtons1.add(button);
		}

		mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		mButtonBack = (ImageView) findViewById(R.id.imageViewBack);
		mButtonForward = (ImageView) findViewById(R.id.imageViewForward);
		mButtonHome = (ImageView) findViewById(R.id.imageViewHome);

		if (!mIsLiteVersion) {
			// Full Version

		} else {
			// Lite Version
			mViewFlipper.setDisplayedChild(0);
			hideNavigationBar();
		}
	}

	private void updateGroupTitle() {
		String[] groupTitle = new String[4];
		groupTitle[0] = mTitleGroup1;
		groupTitle[1] = mTitleGroup2;
		groupTitle[2] = mTitleGroup3;
		groupTitle[3] = mTitleGroup4;

		int currentGroup = mViewFlipper.getDisplayedChild();

		setTitle(groupTitle[currentGroup] != "" ? groupTitle[currentGroup]
				: getResources().getString(R.string.app_name));

	}

	private void hideNavigationBar() {
		mButtonBack.setVisibility(View.GONE);
		mButtonHome.setVisibility(View.GONE);
		mButtonForward.setVisibility(View.GONE);
	}

	private int getMaxShortcutsCount() {
		if (mButtonsCount4 == 0) {
			if (mButtonsCount3 == 0) {
				if (mButtonsCount2 == 0) {
					return MAX_BUTTONS_COUNT1;
				}
				return MAX_BUTTONS_COUNT1 + MAX_BUTTONS_COUNT2;
			}
			return MAX_BUTTONS_COUNT1 + MAX_BUTTONS_COUNT2 + MAX_BUTTONS_COUNT3;
		}
		return MAX_BUTTONS_COUNT1 + MAX_BUTTONS_COUNT2 + MAX_BUTTONS_COUNT3
				+ MAX_BUTTONS_COUNT4;
	}

	private void hideButtons() {
		if (mMaxShortcuts >= MAX_BUTTONS_COUNT1) {
			for (int i = mButtonsCount1; i < MAX_BUTTONS_COUNT1; i++)
				mButtons1.get(i).setVisibility(View.GONE);
		}
		if (mMaxShortcuts >= MAX_BUTTONS_COUNT1 + MAX_BUTTONS_COUNT2) {
			for (int i = MAX_BUTTONS_COUNT1 + mButtonsCount2; i < MAX_BUTTONS_COUNT1
					+ MAX_BUTTONS_COUNT2; i++)
				mButtons1.get(i).setVisibility(View.GONE);
		}
		if (mMaxShortcuts >= MAX_BUTTONS_COUNT1 + MAX_BUTTONS_COUNT2
				+ MAX_BUTTONS_COUNT3) {
			for (int i = MAX_BUTTONS_COUNT1 + MAX_BUTTONS_COUNT2
					+ mButtonsCount3; i < MAX_BUTTONS_COUNT1
					+ MAX_BUTTONS_COUNT2 + MAX_BUTTONS_COUNT3; i++)
				mButtons1.get(i).setVisibility(View.GONE);
		}
		if (mMaxShortcuts == MAX_BUTTONS_COUNT1 + MAX_BUTTONS_COUNT2
				+ MAX_BUTTONS_COUNT3 + MAX_BUTTONS_COUNT4) {
			for (int i = MAX_BUTTONS_COUNT1 + MAX_BUTTONS_COUNT2
					+ MAX_BUTTONS_COUNT3 + mButtonsCount4; i < MAX_BUTTONS_COUNT1
					+ MAX_BUTTONS_COUNT2
					+ MAX_BUTTONS_COUNT3
					+ MAX_BUTTONS_COUNT4; i++)
				mButtons1.get(i).setVisibility(View.GONE);
		}
	}

}