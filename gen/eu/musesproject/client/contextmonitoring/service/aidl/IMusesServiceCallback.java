/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\yasir\\Documents\\GitHub\\MusesClient\\src\\eu\\musesproject\\client\\contextmonitoring\\service\\aidl\\IMusesServiceCallback.aidl
 */
package eu.musesproject.client.contextmonitoring.service.aidl;
public interface IMusesServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback
{
private static final java.lang.String DESCRIPTOR = "eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback interface,
 * generating a proxy if needed.
 */
public static eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback))) {
return ((eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback)iin);
}
return new eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback.Stub.Proxy(obj);
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
case TRANSACTION_onAccept:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onAccept(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onDeny:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onDeny(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements eu.musesproject.client.contextmonitoring.service.aidl.IMusesServiceCallback
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
@Override public void onAccept(java.lang.String response) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(response);
mRemote.transact(Stub.TRANSACTION_onAccept, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onDeny(java.lang.String response) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(response);
mRemote.transact(Stub.TRANSACTION_onDeny, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onAccept = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onDeny = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void onAccept(java.lang.String response) throws android.os.RemoteException;
public void onDeny(java.lang.String response) throws android.os.RemoteException;
}
