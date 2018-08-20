package com.mediatek.wwtv.mediaplayer.mmp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader;
import com.mediatek.wwtv.mediaplayer.mmp.util.BitmapCache;
import com.mediatek.wwtv.mediaplayer.mmp.util.SkyThumbnailUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Author SKY205711
 * Date   2018/1/26
 * Description: This is PreviewListAdapter
 */
public class PreviewListAdapter extends RecyclerView.Adapter<PreviewListAdapter.ViewHolder> {

    private Context mContext;
    private List<FileAdapter> mFileAdapters = new LinkedList<>();

    private int mImgWidth = 212;
    private int mImgHeight = 120;

    private Drawable mVideoDefault;
    private Drawable mAudioDefault;
    private Drawable mPhotoDefault;
    private Drawable mFailedDrable;

    private final Handler mBindHandler;
    private final BitmapCache mCache;
    private final AsyncLoader<Bitmap> mLoader;
    private final ConcurrentHashMap<View, LoadBitmap> mWorks;
    private final ConcurrentHashMap<View, Runnable> mRunnables;

    private OnItemClickListener mOnItemClickListener;

    public PreviewListAdapter(Context mContext) {
        this.mContext = mContext;

        mBindHandler = new Handler();
        mCache = BitmapCache.createCache(false);
        mLoader = AsyncLoader.getInstance(1);
        mWorks = new ConcurrentHashMap<View, LoadBitmap>();
        mRunnables = new ConcurrentHashMap<View, Runnable>();

        Resources res = mContext.getResources();
        mImgWidth = res.getDimensionPixelSize(R.dimen.grid_item_imgwidth);
        mImgHeight = res.getDimensionPixelSize(R.dimen.grid_item_imgheight);

        prepareDefaultThumbnails();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.mmp_item_file, null);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final FileAdapter data = mFileAdapters.get(position);
        String path = data.getAbsolutePath();

        holder.mTextView.setText(data.getName());
        if(data.isVideoFile()) {
            holder.mImageView.setImageDrawable(mVideoDefault);
        } else if(data.isPhotoFile()) {
            holder.mImageView.setImageDrawable(mPhotoDefault);
        } else {
            holder.mImageView.setImageDrawable(mAudioDefault);
        }

        holder.mImageView.setTag(path);
        //bindThumbnail(data, holder.mImageView, path);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClicked(holder, data, position);
                }
            }
        });

        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    holder.mTextView.setVisibility(View.VISIBLE);
                    focusStatus(holder.itemView);
                } else {
                    holder.mTextView.setVisibility(View.INVISIBLE);
                    normalStatus(holder.itemView);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFileAdapters.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.text_title);
            mImageView = itemView.findViewById(R.id.img_preview);
        }
    }

    private void focusStatus(View itemView) {
        if (itemView == null) {
            return;
        }
        itemView.animate().scaleX(1.20f).scaleY(1.20f).translationZ(8).start();
    }

    private void normalStatus(View itemView) {
        if (itemView == null) {
            return;
        }
        itemView.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0).start();
    }

    public void addAll(List<FileAdapter> fileAdapters) {
        if(fileAdapters != null && fileAdapters.size() > 0) {
            int oldCount = mFileAdapters.size();
            mFileAdapters.addAll(fileAdapters);
            notifyItemRangeInserted(oldCount, fileAdapters.size());
        }
    }

    public void clear() {
        mFileAdapters.clear();
        notifyDataSetChanged();
    }

    public FileAdapter getItem(int position) {
        if(position >= 0 && position < mFileAdapters.size()) {
            return mFileAdapters.get(position);
        }
        return null;
    }

    private void prepareDefaultThumbnails() {
        Resources resources = mContext.getResources();
        mVideoDefault = resources
                .getDrawable(R.drawable.mmp_thumbnail_icon_video_middle);
        mAudioDefault = resources
                .getDrawable(R.drawable.mmp_thumbnail_icon_audio_middle);
        mPhotoDefault = resources
                .getDrawable(R.drawable.mmp_thumbnail_icon_photo_middle);
        mFailedDrable = resources
                .getDrawable(R.drawable.mmp_thumbnail_loading_failed_mid);
    }

    private void bindThumbnail(FileAdapter data, ImageView view, String path) {
        Bitmap image = mCache.get(path);
        if (image != null && view.getTag().equals(path)) {
            view.setImageBitmap(image);
        } else {
            if (mWorks.get(view) == null) {
                LoadBitmap work = new LoadBitmap(data, view);
                mWorks.put(view, work);
                mLoader.addWork(work);
            }
        }
    }

    protected void cancel() {
        mLoader.clearQueue();
        mWorks.clear();
    }

    private class LoadBitmap implements AsyncLoader.LoadWork<Bitmap> {
        private final FileAdapter mData;
        private final ImageView vImage;
        private Bitmap mResult;
        private boolean mNeedCache = true;

        public LoadBitmap(FileAdapter data, ImageView iamge) {
            mData = data;
            vImage = iamge;
        }

        public FileAdapter getData() {
            return mData;
        }

        @Override
        public Bitmap load() {
            Bitmap bitmap = null;
            try {
                long start = System.currentTimeMillis();

                if (mData.isVideoFile()) {

                    FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
                    mmr.setDataSource(mData.getAbsolutePath());
                    mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
                    mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
                    bitmap = mmr.getFrameAtTime(-1, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                }

               /* if(mData.isVideoFile()) {
                    bitmap = SkyThumbnailUtils.getVideoThumbnailMINI_KIND(mData.getAbsolutePath(), mImgWidth, mImgHeight);
                } else if(mData.isAudioFile()) {
                    bitmap = SkyThumbnailUtils.getAudioThmbnail(mData.getAbsolutePath());
                } else if(mData.isPhotoFile() || mData.isThrdPhotoFile()) {
                    bitmap = SkyThumbnailUtils.getPictureThumbnail(mData.getAbsolutePath(), mImgWidth, mImgHeight);
                }*/

                if(bitmap == null) {
                    bitmap = mData.getThumbnail(mImgWidth, mImgHeight, true);
                }

                long end = System.currentTimeMillis();
                if (bitmap == null) {
                    if (mData.isPhotoFile()) {
                        bitmap = ((BitmapDrawable) mFailedDrable).getBitmap();
                    } else if (mData.isThrdPhotoFile()) {
                        bitmap = ((BitmapDrawable) mFailedDrable).getBitmap();
                    }
                }
            } catch (OutOfMemoryError e) {
                bitmap = ((BitmapDrawable) mFailedDrable).getBitmap();
                mNeedCache = false;
            }
            mResult = bitmap;
            return bitmap;
        }

        @Override
        public void loaded(Bitmap result) {
            if (result == null) {
                if(mData.isAudioFile()) {
                    mCache.put(mData.getAbsolutePath(),
                            ((BitmapDrawable) mAudioDefault).getBitmap());
                }
                if(mData.isVideoFile()) {
                    mCache.put(mData.getAbsolutePath(),
                            ((BitmapDrawable) mVideoDefault).getBitmap());
                }
            } else if (result != null && mNeedCache) {
                mCache.put(mData.getAbsolutePath(), result);
                mWorks.remove(vImage);
            }

            Runnable r = new LoadBitmap.BindImage();
            mRunnables.put(vImage, r);
            mBindHandler.post(r);
        }

        private class BindImage implements Runnable {
            @Override
            public void run() {
                if (mResult != null) {
                    if (null != vImage.getDrawable()) {
                        if(mData.getAbsolutePath().equals(vImage.getTag())) {
                            vImage.setImageBitmap(mResult);
                        }
                    }
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClicked(ViewHolder holder, FileAdapter data, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

}
