# libjam-mt
Multi touch API for Linux on Java

## What is it?

libjam-mt is a library for linux written in C and Java. It allows for multitouch input in Java for linux systems that use X11. It automatically attaches to the currently focused window and listens for touch events only. It currently is in Beta and is being developed.

## How it works?

It uses libmtdev to emulate kernel input events on the focused windows, which those events via JNI are sent to the TouchHandler Java class which does processing and further forwards it the referenced listeners. The events called are as follows; `onTouch(double x, double y, int id)` is called when the users creates a touch, `onUpdate(double x, double y, int id` is called when the touch of the `id` is still active and has moved, and finally `onRelease(double x, double y, int id)` which is called when the user releases their finger from the screen, of touch id specified in `int id`.

## How to set it up

Installation is fairly complicated but can be done fairly easily. The following explain each step in a fair amount of detail.

##### 1) Add support for multitouch HID devices in your kernel.
You may skip this step if you know you have your drivers installed, if you do not visit [this website](http://lii-enac.fr/en/architecture/linux-input/multitouch-howto.html) for detailed steps on how to configure your kernel.

##### 2) Load in the hid-multitouch modules on boot
In order to use multitouch you must load the module each time at boot. To do this simply add `hid-multitouch` in your `/etc/modules/` files. If you do not have this file or prefer an alternate method, add `sudo modprobe hid-multitouch` to your `/etc/rc.local` file.

The next step is to tell the module what device to use. This can be done by adding `echo W X Y Z > /sys/module/hid_multitouch/drivers/hid\:hid-multitouch/new_id` to the `/etc/rc.local` file after loading in the module where W, X, Y, Z stand for:
###### W – is USB bus number of touchscreen USB, can be figured out by “lsusb” command
###### X and Y – are VID and PID of touchscreen USB, also can be figured out by “lsusb” command
I have a chalk-panel so the Z stood for:
###### Z – is 1 for 7″ and 10″ panels, and 259 for 14″ and 15.6″ panels
If you do not have one of those panels refer to [this website](https://wiki.archlinux.org/index.php/Multitouch_Displays) for details on how to obtain your id.

##### 3) Install required libraries.
`libjam-mt` requires a few libraries to function properly so you will need to install them. Run:
`sudo apt-get install git libmtdev libutouch-frame libutouch-evemu libc`
The package names may vary on your system.

##### 4) Clone this repo
This is easy. `cd` into a directory you like and run `git clone https://github.com/shahbaz-man/libjam-mt.git`. This should produce a folder call `libjam-mt`. Next `cd` into this directory.

##### 5) Run `sudo ./frc`. This should autobuild the native libraries, compile the Java code and run the application. If you did everything right the program should output the X, Y and ID of touches. If you've made it this far then congrats! You can start using the library.

NOTE: If if doesn't work there maybe several reasons:

- The library export path is set to `/t3/lib/`, and you can change this in the `./native/build` script. Do the same in the script `./run` where the `-Djava.library.path` is set to `/t3/lib/`.
- You will need to change the jdk include paths. In the script assemble, replace -I/opt/jdk/include with /path/to/jdk/include and -I/opt/jdk/include/linux to -I/path/to/jdk/include/linux
- In the `./run` script there is a line which calls `export LD_PRELOAD`. This sets the library locations relative to my system and must be change to the locations of where the same libraries reside on your system.
- You may need a reboot.
- You must run as `root` because the C lib needs access to `/dev/input/eventX`
- You may need to change the `/dev/input/eventX` number. You can change this in ./src/com/asdev/libjam/mt/TouchHandler.java in the main method. The line `start(4)` should be replaced with `start(event_device_number)`

NOTE: the `export LD_PRELOAD` is required when running because the C lib can't find them. You should run this line before you start the actual Java application.

#### What do the scripts do?

##### `./compile` should compile the Java code.
##### `./run` should run the Java code.
##### `./cr` should compile and run the Java code.
##### `./native/assemble` should rebuild the native library.
##### `./frc` should rebuild the natives, and call `./cr`.

#### Where can I get help?
Just email me at shahbaz.man3@gmail.com and I will get back to you as soon as I can.
