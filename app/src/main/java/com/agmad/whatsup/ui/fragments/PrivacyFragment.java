package com.agmad.whatsup.ui.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.agmad.whatsup.R;
import com.agmad.whatsup.ui.fragments.base.BasePreFragment;

public class PrivacyFragment extends BasePreFragment {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.fragment_privacy, rootKey);
    }


}
