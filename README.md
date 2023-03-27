[Russian/Русский](README_RUS.md)

## Description

Rutoken DemoBank is a demo application which shows typical usage scenarios of security tokens 
[Rutoken ECP Series](https://www.rutoken.ru/products/all/rutoken-ecp/) family and also contains some useful classes for devices detection and signing docs.

## Requirements

Rutoken DemoBank should be built using:

* Android Studio 3.6 or newer;
* Android SDK Platform 30 or newer.

External dependencies are located in [Rutoken SDK](https://www.rutoken.ru/developers/sdk/).

Required libraries:

* librtpkcs11ecp.so for following architectures: armeabi-v7a, arm64-v8a;
* rtpcsc-\*.aar, where \* is library version.

## How to build

Before building the project:

* copy librtpkcs11ecp.so library to `<project_root>/app/src/main/jniLibs/<arch>/`, where `<arch>` is library architecture;
* copy rtpcsc-\*.aar to `<project_root>/app/libs/`.

To build from console:

    cd <project_root>
    ./gradlew build

To build with Android Studio:

* open Android Studio;
* select `"File">"Open...": <project_root>`;
* select `"Build">"Make project"`.

## Preliminary actions

To create a key pair and certificate on Rutoken ECP Series family devices follow these steps:

* Download and install [Rutoken plugin](https://www.rutoken.ru/products/all/rutoken-plugin/) on your desktop computer;
* Restart your browser to complete plugin installation;
* Go to a [Rutoken register center](https://ra.rutoken.ru) website;
* Connect Rutoken ECP Series family device to your desktop;
    * For Rutoken ECP Bluetooth make sure that only red LED is active (without blue). 
    If not, press and hold the button on device until blue light turns off;
* Make sure that Rutoken ECP Series family device is found by the website;
* Create key pair and certificate, by following instructions on the website;
* Make sure that website has found a certificate and a key pair on your device;
* Disconnect the device from the desktop and use it with Android device.

## Restrictions

* Rutoken DemoBank can only be run on physical devices, not on emulators.

## License

Project source code is distributed under [New BSD license](LICENSE) if the opposite is not mentioned in the source code entity itself,
app/src/main/java/ru/rutoken/demobank directory contains objects of copyright and distributed under commercial license of JSC “Aktiv-Soft”,
[License Agreement](https://download.rutoken.ru/License_Agreement.pdf) (only in Russian).
