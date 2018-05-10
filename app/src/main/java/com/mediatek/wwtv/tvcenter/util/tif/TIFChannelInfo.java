
package com.mediatek.wwtv.tvcenter.util.tif;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;

import java.net.URISyntaxException;

/**
 * @author sin_xinsheng
 */
public class TIFChannelInfo {

  /**
   * When a TIS doesn't provide any information about app link, and it doesn't have a leanback
   * launch intent, there will be no app link card for the TIS.
   */
  public static final int APP_LINK_TYPE_NONE = -1;
  /**
   * When a TIS provide a specific app link information, the app link card will be
   * {@code APP_LINK_TYPE_CHANNEL} which contains all the provided information.
   */
  public static final int APP_LINK_TYPE_CHANNEL = 1;
  /**
   * When a TIS doesn't provide a specific app link information, but the app has a leanback launch
   * intent, the app link card will be {@code APP_LINK_TYPE_APP} which launches the application.
   */
  public static final int APP_LINK_TYPE_APP = 2;

  private static final int APP_LINK_TYPE_NOT_SET = 0;

  public static final int LOAD_IMAGE_TYPE_APP_LINK_ICON = 2;
  public static final int LOAD_IMAGE_TYPE_APP_LINK_POSTER_ART = 3;

  /** sqlite auto increment id, not same as tv api channel id */
  public long mId;
  public String mPackageName = "invalid package name";
  public String mInputServiceName;
  public String mType;
  public String mServiceType;
  public int mOriginalNetworkId;
  public int mTransportStreamId;
  public int mServiceId;
  public String mDisplayNumber;
  public String mDisplayName;
  public String mNetworkAffiliation;
  public String mDescription;
  public String mVideoFormat;
  public boolean mIsBrowsable;
  public boolean mSearchable;
  public boolean mLocked;
  public int mVersionNumber;
  public String mAppLinkIconUri;
  public String mAppLinkPosterArtUri;
  public String mAppLinkText;
  public int mAppLinkColor;
  public String mAppLinkIntentUri;
  public String mData;
  public int mInternalProviderFlag1;
  public int mInternalProviderFlag2;
  public int mInternalProviderFlag3;
  public int mInternalProviderFlag4;
  private Intent mAppLinkIntent;
  private int mAppLinkType;
  /**
   * int mSvlId = Integer.parseInt(value[1]); int mSvlRecId = Integer.parseInt(value[2]); unsigned
   * int mChannelId = Integer.parseInt(value[3]); int mHashcode = Integer.parseInt(value[4]); int
   * mKey = (mSvlId<<16)+mSvlRecId;
   */
  public long mDataValue[];
  public MtkTvChannelInfoBase mMtkTvChannelInfo;

  @Override
  public String toString() {
    return "[TIFChannelInfo] mId:" + mId + ",  mInputServiceName:" + mInputServiceName
        + ",  mType:" + mType + ",  mServiceType:" + mServiceType
        + ",  mOriginalNetworkId:" + mOriginalNetworkId + ",  mTransportStreamId:"
        + mTransportStreamId
        + ",  mServiceId:" + mServiceId + ",  mDisplayNumber:" + mDisplayNumber
        + ",  mDisplayName:" + mDisplayName + ",  mNetworkAffiliation:" + mNetworkAffiliation
        + ",  mDescription:" + mDescription
        + ",  mVideoFormat:" + mVideoFormat + ",  mIsBrowsable:" + mIsBrowsable + ",  mSearchable:"
        + mSearchable
        + ",  mLocked:" + mLocked + ",  mVersionNumber:" + mVersionNumber + ",  mAppLinkIconUri:"
        + mAppLinkIconUri
        + ",  mAppLinkPosterArtUri:" + mAppLinkPosterArtUri + ",  mAppLinkText:" + mAppLinkText
        + ",  mAppLinkColor:" + mAppLinkColor
        + ",  mAppLinkIntentUri:" + mAppLinkIntentUri + ",  mAppLinkIntent:" + mAppLinkIntent
        + ",  mInternalProviderFlag1:"
        + mInternalProviderFlag1 + ",  mInternalProviderFlag2:" + mInternalProviderFlag2
        + ",  mInternalProviderFlag3:" + mInternalProviderFlag3 + ",  mInternalProviderFlag4:"
        + mInternalProviderFlag4
        + ",  mData:" + mData;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof TIFChannelInfo) {
      TIFChannelInfo other = (TIFChannelInfo) o;
      if (other.mId == this.mId) {
        return true;
      }
    }
    return super.equals(o);
  }

  /**
   * Returns the type of app link for this channel. It returns {@link #APP_LINK_TYPE_CHANNEL} if the
   * channel has a non null app link text and a valid app link intent, it returns
   * {@link #APP_LINK_TYPE_APP} if the input service which holds the channel has leanback launch
   * intent, and it returns {@link #APP_LINK_TYPE_NONE} otherwise.
   */
  public int getAppLinkType(Context context) {
    if (mAppLinkType == APP_LINK_TYPE_NOT_SET) {
      initAppLinkTypeAndIntent(context);
    }
    return mAppLinkType;
  }

  /**
   * Returns the app link intent for this channel. If the type of app link is
   * {@link #APP_LINK_TYPE_NONE}, it returns {@code null}.
   */
  public Intent getAppLinkIntent(Context context) {
    if (mAppLinkType == APP_LINK_TYPE_NOT_SET) {
      initAppLinkTypeAndIntent(context);
    }
    return mAppLinkIntent;
  }

  private void initAppLinkTypeAndIntent(Context context) {
    mAppLinkType = APP_LINK_TYPE_NONE;
    mAppLinkIntent = null;
    PackageManager pm = context.getPackageManager();
    if (!TextUtils.isEmpty(mAppLinkText) && !TextUtils.isEmpty(mAppLinkIntentUri)) {
      try {
        Intent intent = Intent.parseUri(mAppLinkIntentUri, Intent.URI_INTENT_SCHEME);
        if (intent.resolveActivityInfo(pm, 0) != null) {
          mAppLinkIntent = intent;
          mAppLinkType = APP_LINK_TYPE_CHANNEL;
          return;
        }
      } catch (URISyntaxException e) {
        // Do nothing.
      }
    }
    if (mPackageName.equals(context.getApplicationContext().getPackageName())) {
      return;
    }
    mAppLinkIntent = pm.getLeanbackLaunchIntentForPackage(mPackageName);
    if (mAppLinkIntent != null) {
      mAppLinkType = APP_LINK_TYPE_APP;
    }
  }

  public Uri getUri() {
    if (true) {
      return TvContract.buildChannelUriForPassthroughInput(mInputServiceName);
    } else {
      return TvContract.buildChannelUri(mId);
    }
  }
}
