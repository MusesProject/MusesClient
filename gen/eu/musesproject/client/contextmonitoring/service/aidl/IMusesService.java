/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\yasir\\Documents\\GitHub\\MusesClient\\src\\eu\\musesproject\\client\\contextmonitoring\\service\\aidl\\IMusesService.aidl
 */
package eu.musesproject.client.contextmonitoring.service.aidl;
public interface IMusesService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements eu.musesproject.client.contextmonitoring.service.aidl.IMusesService
{
private static final java.lang.String DESCRIPTOR = "eu.musesproject.client.contextmonitoring.service.aidl.IMusesService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an eu.musesproject.client.contextmonitoring.service.aidl.IMusesService interface,
 * generating a proxy if needed.
 */
public static eu.musesproject.client.contextmonitoring.service.aidl.IMusesService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof eu.musesproject.client.contextmonitoring.service.aidl.IMusesService))) {
return ((eu.musesproject.client.contextmonitoring.service.aidl.IMusesService)iin);
}
return new eu.musesproject.client.contextmonitoring.service.aidl.IMusesService.Stub.Proxy(obj);
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
case TRANSACTION_sendUserAction:
{
data.enforceInterface(DESCRIPTOR);
eu.musesproject.client.contextmonitoring.service.aidl.Action _arg0;
if ((0!=data.readInt())) {
_arg0 = eu.musesproject.client.contextmonitoring.service.aidl.Action.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.util.Map _arg1;
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_arg1 = data.readHashMap(cl);
this.sendUserAction(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback _arg0;
_arg0 = eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback _arg0;
_arg0 = eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback.Stub.asInterface(data.readStrongBinder());
this.unregisterCallback(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements eu.musesproject.client.contextmonitoring.service.aidl.IMusesService
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
@Override public void sendUserAction(eu.musesproject.client.contextmonitoring.service.aidl.Action action, java.util.Map properties) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((action!=null)) {
_data.writeInt(1);
action.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeMap(properties);
mRemote.transact(Stub.TRANSACTION_sendUserAction, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void registerCallback(eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unregisterCallback(eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_sendUserAction = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void sendUserAction(eu.musesproject.client.contextmonitoring.service.aidl.Action action, java.util.Map properties) throws android.os.RemoteException;
public void registerCallback(eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback callback) throws android.os.RemoteException;
public void unregisterCallback(eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback callback) throws android.os.RemoteException;
}
