package fake.domain.adamlopresto.goshop;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import fake.domain.adamlopresto.goshop.tables.ListsTable;
import fake.domain.adamlopresto.goshop.tables.StoresTable;

public class SettingsFragment extends PreferenceFragment {
	
	public static final String PREF_SHOW_ALL = "pref_show_all";
	public static final String PREF_LIST = "pref_list";
	public static final String PREF_STORE = "pref_store";
	public static final String PREF_SMS_RECIPIENT = "pref_sms_recipient";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        CursorListPreference listPref = (CursorListPreference) preferenceScreen.findPreference(PREF_LIST);
        listPref.setData(ListsTable.TABLE, ListsTable.COLUMN_NAME, false);
        
        CursorListPreference storePref = (CursorListPreference) preferenceScreen.findPreference(PREF_STORE);
        storePref.setData(StoresTable.TABLE, StoresTable.COLUMN_NAME, true);
        
    }
    
}