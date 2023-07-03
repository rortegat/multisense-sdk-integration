# Multisense SDK Integration

The main approach of this integration consist on detect the measurements emitted by Multisense beacons for long periods of time (weeks) without interruptions.

This integration was implemented following the manual provided by Cellocator: [MultiSense SDK Android Java](./MultiSense_SDK_Android_Java v1.1_041218.docx)

## Overview

This project consists on a service `MonitoringService` that is executed in the foreground by creating a notification. The service initiates the BLE scan process provided by the SDK and keeps it running even if the app is minimized or the mobile is locked.

## Run the project

It is required to execute this project in a non-emulated device in order to get access to the physical bluetooth anthena.

It is recommended (not mandatory) to have a Multisense beacon to connect to. The observer will receive the beacons advertisements data and print the quantity of measurements retrieved from each one.



