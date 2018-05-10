package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.SubtitleTrackInfo;
import com.mediatek.wwtv.mediaplayer.R;

import java.util.List;

public class SubtitleAdapter extends RecyclerView.Adapter<SubtitleAdapter.SubtitleViewHolder> {

    private List<SubtitleTrackInfo> mSubtitleTrackInfos;
    private int selectPosition;
    private OnItemClickListener mOnItemClickListener;
    private int mSelectColor;

    public SubtitleAdapter(List<SubtitleTrackInfo> subtitleTrackInfos, int position) {
        this.selectPosition = position;
        this.mSubtitleTrackInfos = subtitleTrackInfos;
    }

    @Override
    public SubtitleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.subtitle_language_list_item, parent, false);
        mSelectColor = context.getResources().getColor(R.color.row_item_background);
        return new SubtitleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SubtitleViewHolder holder, int position) {
        holder.language_tv.setText(mSubtitleTrackInfos.get(position).getSubtitleLanguage());
        holder.subtitle_tv.setText(mSubtitleTrackInfos.get(position).getSubtitleTitle());
        if (selectPosition == position) {
            holder.selectIcon_iv.setVisibility(View.VISIBLE);
        }else {
            holder.selectIcon_iv.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (null == mSubtitleTrackInfos)
            return 0;
        return mSubtitleTrackInfos.size();
    }

    public void setSubtitleTrackInfos(List<SubtitleTrackInfo> subtitleTrackInfos, int position) {
        this.mSubtitleTrackInfos = subtitleTrackInfos;
        this.selectPosition = position;
        notifyDataSetChanged();
    }

    public void setSelectPosition(int position) {
        if (position == selectPosition) {
            return;
        }
        int oldPosition = selectPosition;
        selectPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectPosition);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }

    public class SubtitleViewHolder extends RecyclerView.ViewHolder {

        ImageView selectIcon_iv;
        TextView language_tv;
        TextView subtitle_tv;

        public SubtitleViewHolder(final View itemView) {
            super(itemView);
            selectIcon_iv = itemView.findViewById(R.id.subtitle_select_icon);
            language_tv = itemView.findViewById(R.id.subtitle_lang_name);
            subtitle_tv = itemView.findViewById(R.id.subtitle_txt);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getLayoutPosition();
                    SubtitleTrackInfo trackInfo = mSubtitleTrackInfos.get(position);
                    mOnItemClickListener.onClick(v, position, trackInfo);
                }
            });

            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus){
                        view.setBackgroundColor(mSelectColor);
                    }else {
                        view.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onClick(View view, int position, SubtitleTrackInfo trackInfo);
    }
}
