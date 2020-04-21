package org.but4reuse.adapters.attackTree.preferences;

import org.but4reuse.adapters.attackTree.activator.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initialize preferences
 * 
 * @author MARTEL Benoit, NAVEAU Simon, TRAVAILLE Loïc
 * 
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(AttackTreeAdapterPreferencePage.NODE_ID, "nodeLabel");
		store.setDefault(AttackTreeAdapterPreferencePage.EDGE_ID, "edgeLabel");
	}

}
