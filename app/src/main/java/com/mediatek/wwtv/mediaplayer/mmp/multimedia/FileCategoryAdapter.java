package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.mediatek.wwtv.mediaplayer.R;

import java.util.Locale;
import java.util.List;

/**
 * Created by sniuniu on 2018/3/19.
 */

public class FileCategoryAdapter extends RecyclerView.Adapter<FileCategoryAdapter.FileCategoryViewHolder> {
    private static final String TAG = "FileCategoryAdapter";
    private List<String> mCategory;
    private int mSelectPosition;
    private OnItemFocusedListener mOnItemFocusedListener;
    private boolean isShowSelect = false;
    private boolean setChanged = false;
    private int selectNoFocused;
    private int selectAndFocused;
    private int noSelectNoFocused;


    public FileCategoryAdapter(List<String> category) {
        mCategory = category;
    }

    public void setOnItemFocusedListener(OnItemFocusedListener onItemFocusedListener) {
        mOnItemFocusedListener = onItemFocusedListener;
    }

    @Override
    public FileCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_category_item_layout, parent, false);
        FileCategoryViewHolder holder = new FileCategoryViewHolder(view);
        initColorsResource(parent.getContext());
        return holder;
    }

    @Override
    public void onBindViewHolder(FileCategoryViewHolder holder, int position) {
        holder.mCategoryName_tv.setText(mCategory.get(position));
        if (mSelectPosition == position) {
            holder.mCategoryName_tv.setTextColor(selectNoFocused);
        } else {
            holder.mCategoryName_tv.setTextColor(noSelectNoFocused);
        }
        if (isShowSelect) {
            holder.itemView.setFocusable(false);
        } else {
            holder.itemView.setFocusable(true);
        }

    }

    @Override
    public int getItemCount() {
        if (null == mCategory) {
            return 0;
        }
        return mCategory.size();
    }

    private void initColorsResource(Context context) {
        selectNoFocused = context.getResources().getColor(R.color.select_no_focused);
        selectAndFocused = context.getResources().getColor(R.color.select_and_focused);
        noSelectNoFocused = context.getResources().getColor(R.color.no_select_no_focused);
    }

    public boolean isShowSelect() {
        return isShowSelect;
    }

    public int getSelectPosition() {
        return mSelectPosition;
    }

    public void setShowSelect(boolean isShowSelect) {
        if (this.isShowSelect && !isShowSelect) {
            setChanged = true;
        }
        this.isShowSelect = isShowSelect;
        notifyDataSetChanged();
    }

    public class FileCategoryViewHolder extends RecyclerView.ViewHolder {

        public TextView mCategoryName_tv;

        public FileCategoryViewHolder(View itemView) {
            super(itemView);
            itemView.setEnabled(true);
            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(true);
            mCategoryName_tv = itemView.findViewById(R.id.category_name_text_view);

            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        mSelectPosition = getLayoutPosition();
                        mCategoryName_tv.setTextColor(selectAndFocused);
                        onFocusedStatus(view);
                        if (setChanged) {
                            setChanged = false;
                        } else {
                            if (null != mOnItemFocusedListener) {
                                mOnItemFocusedListener.onItemFocused(view, getLayoutPosition());
                            }
                        }

                    } else {
                        onNormalStatus(view);
                        if (!isShowSelect) {
                            mCategoryName_tv.setTextColor(noSelectNoFocused);
                        } else {
                            mCategoryName_tv.setTextColor(selectNoFocused);
                        }
                    }
                }
            });
        }
    }

    protected void onNormalStatus(View view) {
        if (view == null)
            return;
        ViewCompat.animate(view).scaleX(1.0f).scaleY(1.0f).translationZ(0f).translationX(0).start();
    }

    protected void onFocusedStatus(View view) {
        if (view == null)
            return;
        if (isLayoutRtl()){
            ViewCompat.animate(view).scaleX(1.37f).scaleY(1.37f).translationZ(1f).translationX(-68).start();
        }else {
            ViewCompat.animate(view).scaleX(1.37f).scaleY(1.37f).translationZ(1f).translationX(68).start();
        }
    }

    private boolean isLayoutRtl() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL;
    }

    public interface OnItemFocusedListener {
        void onItemFocused(View view, int position);
    }
}
