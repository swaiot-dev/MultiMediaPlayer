/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\workspace\\android8.0\\MultiMediaPlayer\\mediachannel\\src\\main\\aidl\\com\\mediatek\\wwtv\\mediaplayer\\tvchannel\\ChannelManager.aidl
 */
package com.mediatek.wwtv.mediaplayer.tvchannel;
public interface ChannelManager extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mediatek.wwtv.mediaplayer.tvchannel.ChannelManager
{
private static final java.lang.String DESCRIPTOR = "com.mediatek.wwtv.mediaplayer.tvchannel.ChannelManager";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mediatek.wwtv.mediaplayer.tvchannel.ChannelManager interface,
 * generating a proxy if needed.
 */
public static com.mediatek.wwtv.mediaplayer.tvchannel.ChannelManager asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mediatek.wwtv.mediaplayer.tvchannel.ChannelManager))) {
return ((com.mediatek.wwtv.mediaplayer.tvchannel.ChannelManager)iin);
}
return new com.mediatek.wwtv.mediaplayer.tvchannel.ChannelManager.Stub.Proxy(obj);
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
case TRANSACTION_addPrograms:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.mediatek.wwtv.mediaplayer.tvchannel.ProgramFile> _arg0;
_arg0 = data.createTypedArrayList(com.mediatek.wwtv.mediaplayer.tvchannel.ProgramFile.CREATOR);
this.addPrograms(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack _arg0;
_arg0 = com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack _arg0;
_arg0 = com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack.Stub.asInterface(data.readStrongBinder());
this.unregisterCallback(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mediatek.wwtv.mediaplayer.tvchannel.ChannelManager
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
@Override public void addPrograms(java.util.List<com.mediatek.wwtv.mediaplayer.tvchannel.ProgramFile> programs) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(programs);
mRemote.transact(Stub.TRANSACTION_addPrograms, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void registerCallback(com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unregisterCallback(com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_addPrograms = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void addPrograms(java.util.List<com.mediatek.wwtv.mediaplayer.tvchannel.ProgramFile> programs) throws android.os.RemoteException;
public void registerCallback(com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack cb) throws android.os.RemoteException;
public void unregisterCallback(com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack cb) throws android.os.RemoteException;
}
