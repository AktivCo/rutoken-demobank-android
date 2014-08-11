[ru]
1. Введение
Pkcs11Sample -- это проект, содержащий пример использования Рутокен в устройствах с операционной системой Android через интерфейс PKCS#11 (реализованный в библиотеке librtpkcs11ecp.so) c использованием механизма JNA.

2. Как собрать пример
Внимание! Для успешной сборки примера в проект необходимо установить дополнительные бинарные компоненты: библиотеку librtpkcs11ecp.so (см. п. 3).
Для сборки примера необходимо, чтобы были установлены и настроены Android SDK (http://developer.android.com/sdk/index.html) и система сборки Gradle (http://www.gradle.org/downloads) (подробнее http://developer.android.com/sdk/installing/studio-build.html). 
Требования к среде разработки:
a. Gradle версии 1.10, 1.11 или 1.12 (при сборке из консоли)
b. Android Studio версии 0.8.x
c. Android SDK Build-tools 19.1
d. Android SDK Platform 19

Для сборки из консоли выполните:
~/Pkcs11Sample$> gradle build

Для сборки при помощи Android Studio, откройте проект, используя следующую последовательность обращений к меню:
"File">"Import Project...": "~/Pkcs11Sample/build.gradle"

Для создания проекта в среде Eclipse ADT выполните:
~/Pkcs11Sample$> gradle prepareEclipse
Запустите Eclipse ADT и выполните загрузку проекта, используя следующую последовательность обращений к меню:
"File">"Import">"Android">"Existing android project into workspace">("Next")
"Root directory": "~/Pkcs11Sample/eclipse-project" > ("Finish")


3. Установка/обновление библиотеки librtpkcs11ecp.so
Следует заметить, что, в случае, если пример распространяется вне комплекта разработчика Рутокен (rtSDK), библиотека librtpkcs11ecp.so не распространяется с примером. Расположение библиотеки в  комплекте разработчика Рутокен: Libs/android/native/pkcs11/librtpkcs11ecp.so
Поместите библиотеку librtpkcs11ecp.so по пути ~/Pkcs11Sample/app/libs/librtpkcs11ecp.so.

4. Обновление бинарных модулей поддержки механизма JNA
Для обновления поместите модули поддержки механизма JNA, полученные из дистрибутива JNA (https://github.com/twall/jna/releases) по пути:
~/Pkcs11Sample/app/libs/jna-min.jar
~/Pkcs11Sample/app/deps/android-arm.jar

5. Запуск примера
Для работы примера необходимо, чтобы на мобильном устройстве был установлен сервис Рутокен (можно найти в комплекте разработчика Рутокен (rtSDK) по пути: Services/android/pcsc/service.apk).
Установку собранного примера производите либо средствами Android Sudio, либо при помощи утилиты "adb install"

Замечание
Этот продукт содержит программное обеспечение, разработанное IAIK of Graz University of Technology.

Copyright Aktiv Co. 2014.

[en]
1. Introduction
Pkcs11Sample is a sample project to access Rutoken through rtPKCS11ECP library using JNA.

2. How to build
Warning! To build the sample successfully one has to install additional binary components: rtPKCS11ECP library (see p. 3).
To build Pkcs11Sample you have to get Android SDK (http://developer.android.com/sdk/index.html) and Gradle (http://www.gradle.org/downloads) installed (more information at http://developer.android.com/sdk/installing/studio-build.html).
Build system requirements:
a. Gradle 1.10, 1.11 or 1.12 (if built from console)
b. Android Studio 0.8.x
c. Android SDK Build-tools 19.1
d. Android SDK Platform 19

To build sample from console run:
~/Pkcs11Sample$> gradle build

To build sample from Android Studio, use this option:
"File">"Import Project...": "~/Pkcs11Sample/build.gradle"

To build project with Eclipse ADT run from console:
~/Pkcs11Sample$> gradle prepareEclipse
Then run Eclipse ADT  and open project using following menu calls sequence:
"File">"Import">"Android">"Existing android project into workspace">("Next")
"Root directory": "~/Pkcs11Sample/eclipse-project" > ("Finish")

3. Setting/updating rtPKCS11ECP native library
In case the sample has been distributed without Rutoken SDK, the project does not contain rtPKCS11ECP library. rtPKCS11ECP library may be found in Rutoken SDK at: Libs/android/native/pkcs11/librtpkcs11ecp.so.
To set rtPKCS11ECP library you are to place it to ~/Pkcs11Sample/app/libs/librtpkcs11ecp.so.

4. Updating JNA binaries
To update JNA you are to replace ~/Pkcs11Sample/app/libs/jna-min.jar and ~/Pkcs11Sample/app/deps/android-arm.jar taken from dist directory of JNA distribution (https://github.com/twall/jna/releases).

5. Running sample
For the sample being completely functional be sure to have installed Rutoken service (can be found in Rutoken SDK (rtSDK) at: Services/android/pcsc/service.apk).
Built sample can be installed either with Android Studio or using "adb install" console utility.

Notice
This product includes software developed by IAIK of Graz University of Technology.

Copyright Aktiv Co. 2014.
