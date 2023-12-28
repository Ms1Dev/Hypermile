package com.example.hypermile.util;

import android.view.ViewGroup;

public class Utils {

    /**
     * @brief Sets clipChildren to false recursively on parent ViewGroups
     * @param viewGroup
     */
    static public void unclip(ViewGroup viewGroup) {
        viewGroup.setClipChildren(false);
        viewGroup.setEnabled(false);
        if (viewGroup.getParent() != null) {
            try {
                unclip((ViewGroup) viewGroup.getParent());
            }
            catch (ClassCastException e) {}
        }
    }
}
