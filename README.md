# SugaIOT
SugaIOT is an android application that synchronizes and interacts with a bluetooth low-energy enabled glucose meter. The application implements the 
[Bluetooth Special Interest Group (SIG)](https://www.bluetooth.com/) Glucose meter profile which enables a smartphone to connect and interact with a glucose sensor for consumer health applications. 
> SugaIOT is not yet compatible with BluetoothLe glucose meters that implement the Bluetooth SIG continuous glucose measurement profile.

## Architecture 
SugaIOT was designed with the Model-View-ViewModel (MVVM) architectural pattern which provides a clean archicture for the entire app by laying emphasis on SOLID principles, 
Test Driven Developement (TDD) and Automated Dependency Injection with Dagger Hilt. 

## Bluetooth SIG Glucose Profile Configuration 
SugaIOT's configuration for the bluetooth low-energy glucose profile, services, characteristics and descriptors are managed in the [GlucoseProfileConfiguration](https://github.com/Pekwerike/SugaIOT/blob/master/app/src/main/java/com/example/sugaiot/glucoseprofilemanager/GlucoseProfileConfiguration.kt) object which specifies the relevant:
- 16-bit Universally Unique Identifier (UUID) for glucose service's characteristics and descriptors.
- Opcode, operators, filter types, and response codes for the glucose service record access control point characteristic procedures. 

## Interaction with android bluetooth low-energy APIs 
All major interactions with the android BluetoothLe APIs are done within the [SugaIOTGlucoseProfileManager](https://github.com/Pekwerike/SugaIOT/blob/master/app/src/main/java/com/example/sugaiot/glucoseprofilemanager/SugaIOTGlucoseProfileManager.kt). This class allows for a stable interaction with android bluetooth low-energy APIs and it is responsible for performing all the logical operations that aree neccessary to communicate with the glucose sensor.

## Data Representation
Each glucose measurement record for a patient notified by the glucose sensor Record Access Control Point (RACP) is parse by the SugaIOTGlucoseProfileManager into a [GlucoseMeasurementRecord](https://github.com/Pekwerike/SugaIOT/blob/master/app/src/main/java/com/example/sugaiot/model/GlucoseMeasurementRecord.kt) object. Data notified by the glucose sensor, are parsed using the Bluetooth SIG specifications for the glucose service.



