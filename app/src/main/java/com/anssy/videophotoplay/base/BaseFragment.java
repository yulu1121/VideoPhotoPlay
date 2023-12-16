package com.anssy.videophotoplay.base;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import java.util.Locale;


public class BaseFragment extends Fragment {
    protected Context mContext;

    @Override // androidx.fragment.app.Fragment
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    public String getCurrentLan() {
        Locale locale = getResources().getConfiguration().locale;
        return locale.getLanguage();
    }

}
