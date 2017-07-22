package at.android.speeddiallite;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class Settings extends Activity implements
		SeekBar.OnSeekBarChangeListener, OnItemSelectedListener,
		OnCheckedChangeListener, OnClickListener {

	private static final String PREFS_NAME = "MyPrefsFile";
	private static final int SPINNER_SHORT_CLICK = 0;
	private static final int SPINNER_LONG_CLICK = 1;
	private static final int SPINNER_LAYOUT_SIZE = 2;
	private static final int SPINNER_TEXT_SIZE = 3;
	private static final int SPINNER_TEXT_POSITION = 4;
	private static final int SPINNER_BORDER = 5;
	private static final int SPINNER_CORNER_RADIUS = 6;
	private static final int SPINNER_DEFAULT_IMAGE = 7;

	private CheckBox mCheckBoxHideTitleBar, mCheckBoxHideStatusBar,
			mCheckBoxAutoClose, mCheckBoxShowPlusIcon,
			mCheckBoxSaveLastGroupView, mCheckboxShowNavBar,
			mCheckboxShowNames, mCheckboxAbVc, mCheckboxAbLog, mCheckboxAbCon,
			mCheckboxAbDial, mCheckboxAbAc, mCheckboxAbSc, mCheckboxAbSet;
	private SeekBar mSeekBarShowButtons1, mSeekBarShowButtons2,
			mSeekBarShowButtons3, mSeekBarShowButtons4;
	private TextView mTextViewButtonsCount1, mTextViewButtonsCount2,
			mTextViewButtonsCount3, mTextViewButtonsCount4,
			mTextViewActionbarTitle;
	private Spinner mSpinnerShortClickBehaviour, mSpinnerLongClickBehaviour,
			mSpinnerLayoutSize, mSpinnerTextSize, mSpinnerTextPosition,
			mSpinnerBorder, mSpinnerCornerRadius, mSpinnerDefaultImage;
	private Button mButtonBuyFull, mButtonFeedback, mButtonRateApp;
	private EditText mEditTextTitleGroup1, mEditTextTitleGroup2,
			mEditTextTitleGroup3, mEditTextTitleGroup4;
	private boolean mIsLiteVersion, mIsICSorHigher = false;

	// Global Preferences Variables
	private int mButtonsCount1, mButtonsCount2, mButtonsCount3, mButtonsCount4,
			mShortClickBehavior, mLongClickBehavior, mLayoutSize, mTextSize,
			mTextPosition, mBorderPx, mCornerRadiusPx, mDefaultImage;
	private boolean mHideStatusBar, mHideTitleBar, mAutoClose, mShowPlusIcon,
			mSaveLastGroupView, mShowNavBar, mShowNames, mActionbarVc,
			mActionbarLog, mActionbarCon, mActionbarDial, mActionbarAc,
			mActionbarSc, mActionbarSet;
	private String mTitleGroup1, mTitleGroup2, mTitleGroup3, mTitleGroup4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getTitleName());

		if (Main.DEBUG_STARTUP)
			System.out.println("Settings: onCreate(Bundle savedInstanceState)");

		mIsLiteVersion = this.getPackageName().toLowerCase().contains("lite");
		// mIsLiteVersion = true;

		if (Build.VERSION.SDK_INT >= 14)
			mIsICSorHigher = true;

		setContentBasedOnLayout();
		initializeComponents();
		addComponentListeners();

	}

	/**
	 * Loads Preferences from file and initializes the Settings view objects
	 */
	@Override
	protected void onResume() {
		super.onResume();

		loadProperties();
		setComponentValuesFromProperties();

		if (Main.DEBUG_STARTUP)
			System.out.println("Settings: onResume()");
	}

	/**
	 * Save the Preferences File
	 */
	@Override
	protected void onPause() {
		super.onPause();

		saveProperties();

		if (Main.DEBUG_STARTUP)
			System.out.println("Settings: onPause()");
	}

	private void setContentBasedOnLayout() {

		setContentView(R.layout.settings);

	}

	/**
	 * Inflate the Main Menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_menu, menu);

		return true;
	}

	/**
	 * Callback method from Main Menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_undo:
			setComponentValuesFromProperties();
			return true;
		case R.id.menu_save:
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void initializeComponents() {

		mButtonFeedback = (Button) findViewById(R.id.buttonFeedBack);
		mButtonBuyFull = (Button) findViewById(R.id.buttonBuyFull);
		mButtonRateApp = (Button) findViewById(R.id.buttonRateApp);
		mCheckBoxHideStatusBar = (CheckBox) findViewById(R.id.checkBoxHideStatusBar);
		mCheckBoxHideTitleBar = (CheckBox) findViewById(R.id.checkBoxHideTitleBar);
		mCheckBoxShowPlusIcon = (CheckBox) findViewById(R.id.CheckBoxShowPlusIcon);
		mCheckBoxSaveLastGroupView = (CheckBox) findViewById(R.id.CheckBoxSaveLastGroupView);
		mCheckboxShowNavBar = (CheckBox) findViewById(R.id.CheckBoxShowNavBar);
		mCheckboxShowNames = (CheckBox) findViewById(R.id.CheckBoxShowNames);
		mCheckBoxAutoClose = (CheckBox) findViewById(R.id.checkboxAutoClose);
		mTextViewButtonsCount1 = (TextView) findViewById(R.id.TextViewButtonsCount1);
		mTextViewButtonsCount2 = (TextView) findViewById(R.id.TextViewButtonsCount2);
		mTextViewButtonsCount3 = (TextView) findViewById(R.id.TextViewButtonsCount3);
		mTextViewButtonsCount4 = (TextView) findViewById(R.id.TextViewButtonsCount4);
		mSeekBarShowButtons1 = (SeekBar) findViewById(R.id.SeekBarShowButtons1);
		mSeekBarShowButtons2 = (SeekBar) findViewById(R.id.SeekBarShowButtons2);
		mSeekBarShowButtons3 = (SeekBar) findViewById(R.id.SeekBarShowButtons3);
		mSeekBarShowButtons4 = (SeekBar) findViewById(R.id.SeekBarShowButtons4);
		mSeekBarShowButtons1.setMax(Main.MAX_BUTTONS_COUNT1);
		mSeekBarShowButtons2.setMax(Main.MAX_BUTTONS_COUNT2);
		mSeekBarShowButtons3.setMax(Main.MAX_BUTTONS_COUNT3);
		mSeekBarShowButtons4.setMax(Main.MAX_BUTTONS_COUNT4);
		mSpinnerShortClickBehaviour = (Spinner) findViewById(R.id.spinnerShortClickBehaviour);
		mSpinnerLongClickBehaviour = (Spinner) findViewById(R.id.spinnerLongClickBehaviour);
		mSpinnerLayoutSize = (Spinner) findViewById(R.id.spinnerLayoutSize);
		mSpinnerTextPosition = (Spinner) findViewById(R.id.spinnerTextPosition);
		mSpinnerTextSize = (Spinner) findViewById(R.id.spinnerTextSize);
		mSpinnerBorder = (Spinner) findViewById(R.id.spinnerBorder);
		mSpinnerCornerRadius = (Spinner) findViewById(R.id.spinnerCornerRadius);
		mSpinnerDefaultImage = (Spinner) findViewById(R.id.spinnerDefaultImage);
		mEditTextTitleGroup1 = (EditText) findViewById(R.id.editTextTitleGroup1);
		mEditTextTitleGroup2 = (EditText) findViewById(R.id.editTextTitleGroup2);
		mEditTextTitleGroup3 = (EditText) findViewById(R.id.editTextTitleGroup3);
		mEditTextTitleGroup4 = (EditText) findViewById(R.id.editTextTitleGroup4);
		mCheckboxAbVc = (CheckBox) findViewById(R.id.CheckBox_settings_actionbar_vc);
		mCheckboxAbLog = (CheckBox) findViewById(R.id.CheckBox_settings_actionbar_log);
		mCheckboxAbCon = (CheckBox) findViewById(R.id.CheckBox_settings_actionbar_con);
		mCheckboxAbDial = (CheckBox) findViewById(R.id.CheckBox_settings_actionbar_dial);
		mCheckboxAbAc = (CheckBox) findViewById(R.id.CheckBox_settings_actionbar_ac);
		mCheckboxAbSc = (CheckBox) findViewById(R.id.CheckBox_settings_actionbar_sc);
		mCheckboxAbSet = (CheckBox) findViewById(R.id.CheckBox_settings_actionbar_set);
		mTextViewActionbarTitle = (TextView) findViewById(R.id.TextViewActionbar);

		if (mIsLiteVersion) {
			mButtonBuyFull.setVisibility(View.VISIBLE);

			mCheckBoxSaveLastGroupView.setEnabled(false);
			mCheckboxShowNavBar.setEnabled(false);
			mSeekBarShowButtons2.setEnabled(false);
			mSeekBarShowButtons3.setEnabled(false);
			mSeekBarShowButtons4.setEnabled(false);
			mEditTextTitleGroup2.setEnabled(false);
			mEditTextTitleGroup3.setEnabled(false);
			mEditTextTitleGroup4.setEnabled(false);
		}

		if (mIsICSorHigher) {
			mCheckBoxHideTitleBar.setVisibility(View.GONE);
			mCheckboxAbVc.setVisibility(View.VISIBLE);
			mCheckboxAbLog.setVisibility(View.VISIBLE);
			mCheckboxAbCon.setVisibility(View.VISIBLE);
			mCheckboxAbDial.setVisibility(View.VISIBLE);
			mCheckboxAbAc.setVisibility(View.VISIBLE);
			mCheckboxAbSc.setVisibility(View.VISIBLE);
			mCheckboxAbSet.setVisibility(View.VISIBLE);
			mTextViewActionbarTitle.setVisibility(View.VISIBLE);
		}

		// set Focus to LinearLayout so that EditText is not getting it
		findViewById(R.id.linearLayout1).requestFocus();
	}

	private void loadProperties() {

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

	}

	private void saveProperties() {

		// Global Preferences Variables
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		editor.putBoolean("mHideStatusBar", mCheckBoxHideStatusBar.isChecked());
		editor.putBoolean("mHideTitleBar", mCheckBoxHideTitleBar.isChecked());
		editor.putBoolean("mShowPlusIcon", mCheckBoxShowPlusIcon.isChecked());
		editor.putBoolean("mSaveLastGroupView",
				mCheckBoxSaveLastGroupView.isChecked());
		editor.putBoolean("mShowNavBar", mCheckboxShowNavBar.isChecked());
		editor.putBoolean("mShowNames", mCheckboxShowNames.isChecked());
		editor.putInt("mButtonsCount1v2", mSeekBarShowButtons1.getProgress());
		editor.putInt("mButtonsCount2v2", mSeekBarShowButtons2.getProgress());
		editor.putInt("mButtonsCount3v2", mSeekBarShowButtons3.getProgress());
		editor.putInt("mButtonsCount4v2", mSeekBarShowButtons4.getProgress());
		editor.putInt("mShortClickBehavior", mShortClickBehavior);
		editor.putInt("mLongClickBehavior", mLongClickBehavior);
		editor.putInt("mLayoutSize", mLayoutSize);
		editor.putInt("mTextSizeV2", mTextSize);
		editor.putInt("mTextPosition", mTextPosition);
		editor.putInt("mBorderPx", mBorderPx);
		editor.putInt("mCornerRadiusPx", mCornerRadiusPx);
		editor.putInt("mDefaultImage", mDefaultImage);
		editor.putBoolean("mAutoClose", mCheckBoxAutoClose.isChecked());
		editor.putString("mTitleGroup1", mEditTextTitleGroup1.getText()
				.toString());
		editor.putString("mTitleGroup2", mEditTextTitleGroup2.getText()
				.toString());
		editor.putString("mTitleGroup3", mEditTextTitleGroup3.getText()
				.toString());
		editor.putString("mTitleGroup4", mEditTextTitleGroup4.getText()
				.toString());
		editor.putBoolean("mActionbarVc", mCheckboxAbVc.isChecked());
		editor.putBoolean("mActionbarLog", mCheckboxAbLog.isChecked());
		editor.putBoolean("mActionbarCon", mCheckboxAbCon.isChecked());
		editor.putBoolean("mActionbarDial", mCheckboxAbDial.isChecked());
		editor.putBoolean("mActionbarAc", mCheckboxAbAc.isChecked());
		editor.putBoolean("mActionbarSc", mCheckboxAbSc.isChecked());
		editor.putBoolean("mActionbarSet", mCheckboxAbSet.isChecked());

		editor.commit();
	}

	private void setComponentValuesFromProperties() {

		mCheckBoxHideStatusBar.setChecked(mHideStatusBar);
		mCheckBoxHideTitleBar.setChecked(mHideTitleBar);
		mCheckBoxShowPlusIcon.setChecked(mShowPlusIcon);
		mCheckBoxAutoClose.setChecked(mAutoClose);
		mCheckBoxSaveLastGroupView.setChecked(mSaveLastGroupView);
		mCheckboxShowNavBar.setChecked(mShowNavBar);
		mCheckboxShowNames.setChecked(mShowNames);
		mEditTextTitleGroup1.setText(mTitleGroup1);
		mEditTextTitleGroup2.setText(mTitleGroup2);
		mEditTextTitleGroup3.setText(mTitleGroup3);
		mEditTextTitleGroup4.setText(mTitleGroup4);

		mSeekBarShowButtons1.setProgress(mButtonsCount1);
		mSeekBarShowButtons2.setProgress(mButtonsCount2);
		mSeekBarShowButtons3.setProgress(mButtonsCount3);
		mSeekBarShowButtons4.setProgress(mButtonsCount4);
		// force SeekBars to update on zero value
		if (mButtonsCount1 == 0)
			onProgressChanged(mSeekBarShowButtons1, mButtonsCount1, false);
		if (mButtonsCount2 == 0)
			onProgressChanged(mSeekBarShowButtons2, mButtonsCount2, false);
		if (mButtonsCount3 == 0)
			onProgressChanged(mSeekBarShowButtons3, mButtonsCount3, false);
		if (mButtonsCount4 == 0)
			onProgressChanged(mSeekBarShowButtons4, mButtonsCount4, false);

		// Short Click Spinner
		mSpinnerShortClickBehaviour.setPrompt(this.getResources().getString(
				R.string.settings_shortClickBehavoir));
		mSpinnerShortClickBehaviour
				.setAdapter(getSpinnerAdapter(Settings.SPINNER_SHORT_CLICK));
		mSpinnerShortClickBehaviour.setSelection(mShortClickBehavior);

		// Long Click Spinner
		mSpinnerLongClickBehaviour.setPrompt(this.getResources().getString(
				R.string.settings_longClickBehavoir));
		mSpinnerLongClickBehaviour
				.setAdapter(getSpinnerAdapter(Settings.SPINNER_LONG_CLICK));
		mSpinnerLongClickBehaviour.setSelection(mLongClickBehavior);

		// Layout Size
		mSpinnerLayoutSize.setPrompt(this.getResources().getString(
				R.string.settings_layout));
		mSpinnerLayoutSize
				.setAdapter(getSpinnerAdapter(Settings.SPINNER_LAYOUT_SIZE));
		mSpinnerLayoutSize.setSelection(mLayoutSize);

		// Text Size
		mSpinnerTextSize.setPrompt(this.getResources().getString(
				R.string.settings_textSize));
		mSpinnerTextSize
				.setAdapter(getSpinnerAdapter(Settings.SPINNER_TEXT_SIZE));
		mSpinnerTextSize.setSelection(mTextSize);

		// Text Position
		mSpinnerTextPosition.setPrompt(this.getResources().getString(
				R.string.settings_textPosition));
		mSpinnerTextPosition
				.setAdapter(getSpinnerAdapter(Settings.SPINNER_TEXT_POSITION));
		mSpinnerTextPosition.setSelection(mTextPosition);

		// Border Size
		mSpinnerBorder.setPrompt(this.getResources().getString(
				R.string.settings_border));
		mSpinnerBorder.setAdapter(getSpinnerAdapter(Settings.SPINNER_BORDER));
		mSpinnerBorder.setSelection(mBorderPx);

		// Corner Radius of Border
		mSpinnerCornerRadius.setPrompt(this.getResources().getString(
				R.string.settings_cornerradius));
		mSpinnerCornerRadius
				.setAdapter(getSpinnerAdapter(Settings.SPINNER_CORNER_RADIUS));
		mSpinnerCornerRadius.setSelection(mCornerRadiusPx);

		// Default Image
		mSpinnerDefaultImage.setPrompt(this.getResources().getString(
				R.string.settings_defaultimage));
		mSpinnerDefaultImage
				.setAdapter(getSpinnerAdapter(Settings.SPINNER_DEFAULT_IMAGE));
		mSpinnerDefaultImage.setSelection(mDefaultImage);

		// Action Bar
		mCheckboxAbVc.setChecked(mActionbarVc);
		mCheckboxAbLog.setChecked(mActionbarLog);
		mCheckboxAbCon.setChecked(mActionbarCon);
		mCheckboxAbDial.setChecked(mActionbarDial);
		mCheckboxAbAc.setChecked(mActionbarAc);
		mCheckboxAbSc.setChecked(mActionbarSc);
		mCheckboxAbSet.setChecked(mActionbarSet);

	}

	private ArrayAdapter<CharSequence> getSpinnerAdapter(int component) {

		ArrayAdapter<CharSequence> spinnerArrayAdapter;

		switch (component) {
		case SPINNER_SHORT_CLICK:
			spinnerArrayAdapter = new ArrayAdapter<CharSequence>(
					this,
					android.R.layout.simple_spinner_item,
					new CharSequence[] {
							getResources().getString(
									R.string.contextmenu_viewContact),
							getResources().getString(R.string.contextmenu_call),
							getResources().getString(
									R.string.contextmenu_sendmsg),
							getResources().getString(R.string.contextmenu_dial),
							getResources().getString(R.string.contextmenu_copy),
							getResources().getString(
									R.string.contextmenu_callmenu) });
			spinnerArrayAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			return spinnerArrayAdapter;

		case SPINNER_LONG_CLICK:
			spinnerArrayAdapter = new ArrayAdapter<CharSequence>(
					this,
					android.R.layout.simple_spinner_item,
					new CharSequence[] {
							getResources().getString(R.string.contextmenu_cm),
							getResources().getString(
									R.string.contextmenu_viewContact),
							getResources().getString(R.string.contextmenu_call),
							getResources().getString(
									R.string.contextmenu_sendmsg),
							getResources().getString(R.string.contextmenu_dial),
							getResources().getString(R.string.contextmenu_copy),
							getResources().getString(R.string.contextmenu_none),
							getResources().getString(
									R.string.contextmenu_callmenu) });

			spinnerArrayAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			return spinnerArrayAdapter;

		case SPINNER_LAYOUT_SIZE:
			spinnerArrayAdapter = new ArrayAdapter<CharSequence>(this,
					android.R.layout.simple_spinner_item, new CharSequence[] {
							getResources().getString(
									R.string.settings_layout_small),
							getResources().getString(
									R.string.settings_layout_medium),
							getResources().getString(
									R.string.settings_layout_large) });

			spinnerArrayAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			return spinnerArrayAdapter;

		case SPINNER_TEXT_SIZE:
			// spinnerArrayAdapter = new ArrayAdapter<CharSequence>(this,
			// android.R.layout.simple_spinner_item, new CharSequence[] {
			// getResources().getString(
			// R.string.settings_textSize_xsmall),
			// getResources().getString(
			// R.string.settings_textSize_small),
			// getResources().getString(
			// R.string.settings_textSize_medium),
			// getResources().getString(
			// R.string.settings_textSize_large),
			// getResources().getString(
			// R.string.settings_textSize_xlarge) });

			List<CharSequence> items3 = new ArrayList<CharSequence>();
			for (int i = 10; i <= 30; i += 1)
				items3.add(Integer.toString(i));

			spinnerArrayAdapter = new ArrayAdapter<CharSequence>(this,
					android.R.layout.simple_spinner_item, items3);
			spinnerArrayAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			return spinnerArrayAdapter;

		case SPINNER_TEXT_POSITION:
			spinnerArrayAdapter = new ArrayAdapter<CharSequence>(this,
					android.R.layout.simple_spinner_item, new CharSequence[] {
							getResources().getString(
									R.string.settings_textPosition_top),
							getResources().getString(
									R.string.settings_textPosition_center),
							getResources().getString(
									R.string.settings_textPosition_bottom) });

			spinnerArrayAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			return spinnerArrayAdapter;

		case SPINNER_BORDER:
			List<CharSequence> items = new ArrayList<CharSequence>();
			for (int i = 0; i <= 20; i += 1)
				items.add(Integer.toString(i));

			spinnerArrayAdapter = new ArrayAdapter<CharSequence>(this,
					android.R.layout.simple_spinner_item, items);

			spinnerArrayAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			return spinnerArrayAdapter;

		case SPINNER_CORNER_RADIUS:
			List<CharSequence> items2 = new ArrayList<CharSequence>();
			for (int i = 0; i <= 40; i += 2)
				items2.add(Integer.toString(i));

			spinnerArrayAdapter = new ArrayAdapter<CharSequence>(this,
					android.R.layout.simple_spinner_item, items2);

			spinnerArrayAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			return spinnerArrayAdapter;

		case SPINNER_DEFAULT_IMAGE:

			spinnerArrayAdapter = new ArrayAdapter<CharSequence>(this,
					android.R.layout.simple_spinner_item, new CharSequence[] {
							"Droid gray", "Droid  green", "Droid yellow",
							"Droid red", "Droid pink", "Droid blue",
							"Contact Icon", "None" });

			spinnerArrayAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			return spinnerArrayAdapter;

		default:
			return null;
		}

	}

	private void addComponentListeners() {
		mButtonBuyFull.setOnClickListener(this);
		mButtonFeedback.setOnClickListener(this);
		mButtonRateApp.setOnClickListener(this);
		mSeekBarShowButtons1.setOnSeekBarChangeListener(this);
		mSeekBarShowButtons2.setOnSeekBarChangeListener(this);
		mSeekBarShowButtons3.setOnSeekBarChangeListener(this);
		mSeekBarShowButtons4.setOnSeekBarChangeListener(this);
		// mCheckBoxShowPlusIcon.setOnCheckedChangeListener(this);
		mSpinnerShortClickBehaviour.setOnItemSelectedListener(this);
		mSpinnerLongClickBehaviour.setOnItemSelectedListener(this);
		mSpinnerLayoutSize.setOnItemSelectedListener(this);
		mSpinnerTextPosition.setOnItemSelectedListener(this);
		mSpinnerTextSize.setOnItemSelectedListener(this);
		mSpinnerBorder.setOnItemSelectedListener(this);
		mSpinnerCornerRadius.setOnItemSelectedListener(this);
		mSpinnerDefaultImage.setOnItemSelectedListener(this);

	}

	/**
	 * Callback methods for SeekBar View Component
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		switch (seekBar.getId()) {
		case R.id.SeekBarShowButtons1:
			mTextViewButtonsCount1.setText(getResources().getString(
					R.string.settings_buttonsCount1)
					+ ": " + String.valueOf(progress));

			mEditTextTitleGroup1.setEnabled(progress == 0 ? false : true);
			break;
		case R.id.SeekBarShowButtons2:
			mTextViewButtonsCount2.setText(getResources().getString(
					R.string.settings_buttonsCount2)
					+ ": " + String.valueOf(progress));

			mEditTextTitleGroup2.setEnabled(progress == 0 ? false : true);
			break;
		case R.id.SeekBarShowButtons3:
			mTextViewButtonsCount3.setText(getResources().getString(
					R.string.settings_buttonsCount3)
					+ ": " + String.valueOf(progress));

			mEditTextTitleGroup3.setEnabled(progress == 0 ? false : true);
			break;
		case R.id.SeekBarShowButtons4:
			mTextViewButtonsCount4.setText(getResources().getString(
					R.string.settings_buttonsCount4)
					+ ": " + String.valueOf(progress));

			mEditTextTitleGroup4.setEnabled(progress == 0 ? false : true);
			break;
		default:
			break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

		switch (arg0.getId()) {
		case R.id.spinnerShortClickBehaviour:
			this.mShortClickBehavior = arg2;
			break;

		case R.id.spinnerLongClickBehaviour:
			this.mLongClickBehavior = arg2;
			break;

		case R.id.spinnerLayoutSize:
			this.mLayoutSize = arg2;
			break;

		case R.id.spinnerTextPosition:
			this.mTextPosition = arg2;
			break;

		case R.id.spinnerTextSize:
			this.mTextSize = arg2;
			break;

		case R.id.spinnerBorder:
			this.mBorderPx = arg2;
			break;

		case R.id.spinnerCornerRadius:
			this.mCornerRadiusPx = arg2;
			break;

		case R.id.spinnerDefaultImage:
			this.mDefaultImage = arg2;
			break;

		default:
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// System.out.println("nothing selected");
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonBuyFull:
			buyFullVersion();
			break;
		case R.id.buttonFeedBack:
			feedback();
			break;
		case R.id.buttonRateApp:
			rateThisApplication();
			break;
		default:
			break;
		}
	}

	/**
	 * Contact the developer
	 */
	private void feedback() {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		// emailIntent.setType("plain/text");
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { getResources().getString(R.string.dev_email) });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				this.getPackageName());
		startActivity(Intent.createChooser(emailIntent, "Send mail..."));

		this.finish();
	}

	/**
	 * open Market and show Full Version of SpeedDial
	 */
	private void buyFullVersion() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		String uri = "market://details?id=at." + "android.spee" + "ddial";
		intent.setData(Uri.parse(uri));
		startActivity(intent);

		this.finish();
	}

	/**
	 * open Market and show application details
	 */
	private void rateThisApplication() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		String uri = "market://details?id=" + this.getPackageName();
		intent.setData(Uri.parse(uri));
		startActivity(intent);

		this.finish();
	}

	private String getTitleName() {
		String versionName = null;
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(),
					0).versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// return getResources().getString(R.string.menu_settings) + " ("
		// + getResources().getString(R.string.app_name) + " - Version: "
		// + versionName + ")";
		return getResources().getString(R.string.menu_settings) + " (v"
				+ versionName + ")";
	}
}
