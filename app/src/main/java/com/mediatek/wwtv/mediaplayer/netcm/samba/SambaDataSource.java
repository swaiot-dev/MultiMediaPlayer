
package com.mediatek.wwtv.mediaplayer.netcm.samba;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

/**
 * Auxiliary class for Samba manager.
 * Use to create samba input stream.
 *
 */
public class SambaDataSource {
  private final SmbFile mSmbFile;

  public SambaDataSource(SmbFile smbFile) {
    this.mSmbFile = smbFile;
  }

  public InputStream newInputStream() throws SmbException, MalformedURLException,
      UnknownHostException {
    return new SmbFileInputStream(mSmbFile);
  }
}
