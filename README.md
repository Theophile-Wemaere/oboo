# The `Oboo` Android application

This repository contains the source code of the `Oboo` Android mobile application.
This application makes use of our own web API (`Oboo API`) which exposes endpoints that contain information about ISEP buildings.

`Oboo` uses the following technologies:
- [Jetpack Compose](https://developer.android.com/compose)
- [Room](https://developer.android.com/jetpack/androidx/releases/room)
- [Retrofit](https://square.github.io/retrofit/)
- [Compose Charts](https://ehsannarmani.github.io/ComposeCharts/)
- [Oboo API](https://github.com/Theophile-Wemaere/oboo-api)

This mobile application is made available only to ISEP students, personnel and contractors (that have access to a valid ISEP email address).
Even though authentication is enforced at the API level, you can build your own web API and change the URL used by `Oboo` in the `RetrofitInstance.kt` file.

## Development instructions

To start developing on this project, simply clone this repository and open it in [Android Studio](https://developer.android.com/studio).

## Running `Oboo`

To run the app on an emulator or on your own phone, simply build and run the projet in [Android Studio](https://developer.android.com/studio).

On first launch, you will be prompted to enter your ISEP email. If the provided email is valid, you will receive a 6-digit code on your email address with a validity of 10 minutes.
Once your enter a valid code, you will be given an API key (valid for 30 days) allowing the application to communicate with the `Oboo API`.

You can now browse the different pages to explore the various buildings, floor and rooms of ISEP !

![](https://i.imgur.com/f3ZWU7L.png)
