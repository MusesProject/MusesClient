MusesClient
===========

This is the Client part of the [Muses](http://musesproject.eu) suite, containing sources for the Android version of the MUSES Client, which interacts with operating system, applications and MUSES Server in order to protect the device and enforce corporate policies. 


##License

This package is released under the Affero GPL license, although you can use parts of it under a different license. Read [LICENSE](LICENSE) for details.

## Documentation

There is a [separate repo](https://github.com/MusesProject/Muses-Developer-Guide) for the developer guide. You can download a PDF from the [releases section](https://github.com/MusesProject/Muses-Developer-Guide/releases) or build your own.

## Build

You will have to build [Muses common](https://github.com/MusesProject/MusesCommon) module first. Next, you have to import this project as an Android project (e.g. with ADT Eclipse).

At the end you should have the following classpath:

<?xml version="1.0" encoding="UTF-8"?>
<classpath>
	<classpathentry kind="src" path="src"/>
	<classpathentry kind="src" path="gen"/>
	<classpathentry kind="con" path="com.android.ide.eclipse.adt.ANDROID_FRAMEWORK"/>
	<classpathentry exported="true" kind="con" path="com.android.ide.eclipse.adt.LIBRARIES"/>
	<classpathentry exported="true" kind="con" path="com.android.ide.eclipse.adt.DEPENDENCIES"/>
	<classpathentry kind="lib" path="libs/common-0.0.1-SNAPSHOT.jar"/>
	<classpathentry kind="output" path="bin/classes"/>
</classpath>


