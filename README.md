# SugaIOT
SugaIOT is an android application that synchronizes and interacts with a bluetooth low-energy enabled glucose meter. The application implements the 
Bluetooth Special Interest Group (SIG) Glucose meter profile which enables a smartphone to connect and interact with a glucose sensor for consumer health applications. 

## Architecture 
SugaIOT was designed with the Model-View-ViewModel (MVVM) architectural pattern which provides a clean archicture for the entire app by laying emphasis on SOLID principles, 
Test Driven Developement (TDD) and Automated Dependency Injection. 

## Bluetooth SIG Glucose Profile Configuration 
SugaIOT's configuration for the bluetooth low-energy glucose profile, services, characteristics and descriptors are managed in the GlucoseProfileConfiguration object which specifies the relevant
- 16-bit Universally Unique Identifier (UUID) for glucose service's characteristics and descriptors.
- Opcode, operators, filter types, and response codes for the glucose service record access control point characteristic procedures. 

## Interaction with android bluetooth low-energy APIs 
All major interactions with the android BluetoothLe APIs are done within the <l href="https://github.com/Pekwerike/SugaIOT/blob/master/app/src/main/java/com/example/sugaiot/service/SugaIOTBluetoothLeService.kt">SugaIOTBluetoothLeService</l>. This class allows for a stable interaction with android bluetooth low-energy
APIs irrespective of the current activity context. Once the application is started the MainActivity binds to this service and unbinds from the service when the activity is destroyed 
but the service continues running in the foreground. 

## Producers-Subcriber message transmission pattern with the BluetoothGattStateInformationReceiver local broadcast receiver



