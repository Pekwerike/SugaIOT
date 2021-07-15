# SugaIOT
SugaIOT is an android application that synchronizes and interacts with a Bluetooth low-energy glucose meter. The application implements the 
[Bluetooth Special Interest Group (SIG)](https://www.bluetooth.com/) Glucose meter profile which enables a smartphone to connect and interact with a glucose sensor for consumer health applications. 
> SugaIOT is not yet compatible with glucose meters that implement the Bluetooth SIG Continuous Glucose Measurement (CGM) profile and SugaIOT application cannot run on an android emulator since it doesn't have a Bluetooth Adapter.

## Architecture 
SugaIOT was designed with the Model-View-ViewModel (MVVM) architectural pattern which provides a clean architecture for the entire app by laying emphasis on SOLID principles, 
Test-Driven Development (TDD) and Automated Dependency Injection with Dagger Hilt. 

## Bluetooth SIG Glucose Profile Configuration 
SugaIOT's configuration for the Bluetooth low-energy glucose profile, services, characteristics, and descriptors are managed in the [GlucoseProfileConfiguration](https://github.com/Pekwerike/SugaIOT/blob/master/app/src/main/java/com/example/sugaiot/glucoseprofilemanager/GlucoseProfileConfiguration.kt) object which specifies the relevant:
- 16-bit Universally Unique Identifier (UUID) for glucose service's characteristics and descriptors.
- Opcode, operators, filter types, and response codes for the glucose service Record Access Control Point (RACP) characteristic procedures. 

## Interaction with android Bluetooth low-energy APIs 
All major interactions with the android BluetoothLe APIs are done within the [SugaIOTGlucoseProfileManager](https://github.com/Pekwerike/SugaIOT/blob/master/app/src/main/java/com/example/sugaiot/glucoseprofilemanager/SugaIOTGlucoseProfileManager.kt). This class allows for a stable interaction with android Bluetooth low-energy APIs and it is responsible for performing all the logical operations that are necessary to communicate with the glucose sensor.

## Data Representation
Glucose measurement record for a patient is read from the glucose sensor RACP and parsed by the SugaIOTGlucoseProfileManager into a [GlucoseMeasurementRecord](https://github.com/Pekwerike/SugaIOT/blob/master/app/src/main/java/com/example/sugaiot/model/GlucoseMeasurementRecord.kt) object. Glucose measurement record data parsing and decrypting are done by following the Bluetooth SIG specifications for the glucose service.

## Permission Requirements 
SugaIOT requires the following runtime permission from the user 
1. Access to turn on the device GPS 
2. Access to device turn on the device bluetooth

## ScreenShot
 <img src="https://user-images.githubusercontent.com/43956851/116695390-78c22c80-a9b8-11eb-9b0e-76f3d3caec2b.jpg" width="320" height="620">

## Demo 
[Youtube video demo](https://www.youtube.com/watch?v=BSC11n1Fvz4)
## How to Install 
Clone the repository and open the project in any Android Studio version >= 4.0 




