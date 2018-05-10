package com.mediatek.wwtv.mediaplayer.mmp.model;

import android.content.Context;
import android.content.res.Resources;

import com.mediatek.wwtv.mediaplayer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sniuniu on 2018/3/19.
 */

public class FileCategoryManager {

    private List<String> mCategory;

    public FileCategoryManager(Context context) {
        init(context);
    }

    private void init(Context context) {
        if (null == mCategory) {
            mCategory = new ArrayList<>();
        }
        Resources resources = context.getResources();
        String[] fileCategorys = resources.getStringArray(R.array.file_category);
        for (String category : fileCategorys) {
            mCategory.add(category);
        }
    }

    public List<String> getCategory() {
        return mCategory;
    }
}
