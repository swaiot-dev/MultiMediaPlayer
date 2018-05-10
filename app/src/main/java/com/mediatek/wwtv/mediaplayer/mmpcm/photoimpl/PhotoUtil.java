package com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl;

import android.graphics.Bitmap;
import android.graphics.Movie;
import android.util.Log;

public class PhotoUtil {
	
	private Bitmap mBitmap;
	private Movie mMovie;
	
	
	
	public PhotoUtil(Bitmap bitmap) {
		super();
		this.mBitmap = bitmap;
		
	}
	
	
	public PhotoUtil() {
		super();
		// TODO Auto-generated constructor stub
	}
    

	public PhotoUtil(Movie movie) {
		super();
		this.mMovie = movie;
	}

	public void setBitmap(Bitmap bitmap){
		this.mBitmap = bitmap;
		this.mMovie = null;
		
	}

	public void setMovie(Movie movie){
		this.mBitmap = null;
		this.mMovie = movie;
	}


	public Bitmap getBitmap(){
		return mBitmap;
	}

	public Movie getMovie(){
		return mMovie;
	}
	

}
