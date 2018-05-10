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

public class AudioTrackAdapter extends RecyclerView.Adapter<AudioTrackAdapter.AudioTrackViewHolder> {

    private List<String> mAudioTrackInfos;
    private int selectPosition;
    private OnItemClickListener mOnItemClickListener;
    private int mSelectColor;

    public AudioTrackAdapter(List<String> audioTrackInfos, int position) {
        this.selectPosition = position;
        this.mAudioTrackInfos = audioTrackInfos;
    }

    @Override
    public AudioTrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.audio_language_list_item, parent, false);
        mSelectColor = context.getResources().getColor(R.color.row_item_background);
        return new AudioTrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AudioTrackViewHolder holder, int position) {
        String track = mAudioTrackInfos.get(position);
        holder.language_tv.setText(track);
        if (track.contains("dolby")) {
            holder.dolbyIcon_iv.setVisibility(View.VISIBLE);
            holder.musicIcon_tv.setVisibility(View.INVISIBLE);
        } else {
            holder.musicIcon_tv.setVisibility(View.VISIBLE);
            holder.dolbyIcon_iv.setVisibility(View.INVISIBLE);
        }
        if (selectPosition == position) {
            holder.selectIcon_iv.setVisibility(View.VISIBLE);
        } else {
            holder.selectIcon_iv.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (null == mAudioTrackInfos)
            return 0;
        return mAudioTrackInfos.size();
    }

    public void setAudioTrackInfos(List<String> subtitleTrackInfos, int position) {
        this.mAudioTrackInfos = subtitleTrackInfos;
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

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public class AudioTrackViewHolder extends RecyclerView.ViewHolder {

        ImageView selectIcon_iv;
        TextView language_tv;
        ImageView dolbyIcon_iv;
        ImageView musicIcon_tv;

        public AudioTrackViewHolder(final View itemView) {
            super(itemView);
            selectIcon_iv = itemView.findViewById(R.id.audio_select_icon);
            language_tv = itemView.findViewById(R.id.audio_lang_name);
            dolbyIcon_iv = itemView.findViewById(R.id.audio_dolby_icon);
            musicIcon_tv = itemView.findViewById(R.id.audio_iv_icon);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getLayoutPosition();
                    String audioTrack = mAudioTrackInfos.get(position);
                    mOnItemClickListener.onClick(v, position, audioTrack);
                }
            });

            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        language_tv.setSelected(true);
                        view.setBackgroundColor(mSelectColor);
                    } else {
                        language_tv.setSelected(false);
                        view.setBackgroundColor(Color.TRANSPARENT);
                    }

                }
            });
        }
    }

    public interface OnItemClickListener {
        void onClick(View view, int position, String trackInfo);
    }
}
