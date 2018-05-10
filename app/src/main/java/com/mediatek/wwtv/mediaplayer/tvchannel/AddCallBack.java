/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\workspace\\android8.0\\MultiMediaPlayer\\mediachannel\\src\\main\\aidl\\com\\mediatek\\wwtv\\mediaplayer\\tvchannel\\AddCallBack.aidl
 */
package com.mediatek.wwtv.mediaplayer.tvchannel;
public interface AddCallBack extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack
{
private static final java.lang.String DESCRIPTOR = "com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack interface,
 * generating a proxy if needed.
 */
public static com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack))) {
return ((com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack)iin);
}
return new com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_finishedAddProgram:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.finishedAddProgram(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void finishedAddProgram(boolean success) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((success)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_finishedAddProgram, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_finishedAddProgram = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void finishedAddProgram(boolean success) throws android.os.RemoteException;
}
