SDP-Project-Group-9
===================

BrickProject is the client program to run on an NXT brick flashed with LeJOS 0.9.1.

PcProject is the server application to run on a DICE machine with a usb bluetooth dongle inserted. To start the server you should run the RunVision class in Eclipse.

SharedLib is simply a library project that is shared between the Brick and Pc Projects.

In order to get the v4l4j library to see the libvideo.so.0 shared object file you will need to change LD_LIBRARY_PATH to point to the folder on your DICE login that contains the shared object file (namely the lib folder of the PcProject). This can either be done within Eclipse by altering the Run Configuration for RunVision, or outside of Eclipse by creating a new shortcut to Eclipse 4.2 with "env LD_LIBRARY_PATH=[your path to file] /opt/eclipse4.2/eclipse" as the command.

Note: The final presentation was created in Office 2013, and will NOT show correctly in LibreOffice or the like.
