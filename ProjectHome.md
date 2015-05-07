# XPad Server: A Server for the XBox 360 Gamepad and PS3 SixAxis Controllers #
XPad Server is a Windows TCP Server that allows any WIFI enabled mobile device (phone or tablet) to access an XBOX 360 Gamepad controller attached to a PC. XPad Server relays the XBOX 360 controller events to a TCP client thus allowing any type of mobile device: IPhone, IPad, Android, etc to use the XBOX Gamepad controller. Good for FPS games.

XPad Server runs in the background listening for TCP connections:

![http://xpad-server.googlecode.com/files/st1.jpg](http://xpad-server.googlecode.com/files/st1.jpg)

Once a client connects (mobile device over WIFI), simply plug-in your XBOX 360 Gamepad controller to the PC, select **Connect USB XBox Controller** from the menu and XPadServer will start relaying the the XInput events back to the client:

![http://xpad-server.googlecode.com/files/st2.jpg](http://xpad-server.googlecode.com/files/st2.jpg)

### Why a Gamepad Server? ###
The XBOX 360 controller uses its own transport API incompatible with any mobile device (phone or tablet). XPad Server allows your mobile device to receive Gamepad events from a controller attached to your PC.

### Why not Bluetooth? ###
Most mobile devices: IPhone, IPad, Touchpad, Blackberry have crippled BT stacks. Android has a decent stack but it is not capable of gamepad input. Using WIFI you can use basic TCP sockets to receive Gamepad events from the server.


## Requirements ##
  * PC with the latest DirectX run time: see http://www.microsoft.com/download/en/details.aspx?id=9033
  * Wired XBOX Gamepad controller: MadCatz or Xbox 360 Controller for Windows

# Setting up an XBOX 360 Gamepad Controller with your PC #

This section will show you how to get an XBOX 360 Gamepad setup with your PC. Skip it if you already know how. We'll use the standard Madcatz Gamepad for Xbox 360:

  * Install the latest DirectX runtime from Microsoft (see requirements)
  * Install the XBOX 360 Gamepad driver from Microsoft: http://www.microsoft.com/hardware/en-us/d/xbox-360-controller-for-windows
  * Reboot and plug-in your XBOX 360 Gamepad controller
  * You are all set! Run the XPad server, right click on the systray icon and tick **Connect USB XBOX Controller**

# Setting up an PS3 Controller with your PC #

You can use your PS3 Controller with the help of MotioninJoy with optional wireless support (if you have a compatible bluetooh dongle). This wonderful software can translate PS3 controller events into XINPUT (X360) events thus allowing you to use the PS3 controller with our server. To do this you will need:

  * Install MotioninJoy
  * Follow the instructions [here](http://code.google.com/p/xpad-server/wiki/PS3Tutorial).
  * Optional bluetooth dongle for wireless.

## Compatible Gamepads ##
  * Madcatz Gamepad for Xbox 360
  * Xbox 360 Controller for Windows (from MS hardware)
  * PS3 SixAxis with MotioninJoy (optional wireless w/ a bluetooth dongle)

# Testing with Android #
We provide an Input Method (IME) Android app to test your XBOX Controller.
  * Plug-in the XBox controller to your PC, Run the XPad Server, and connect to the controller.
  * Install the IME APK in your phone or tablet and enable the **XBox 360 Controller** input method under **Settings, Language and Keyboard** in your phone
![http://xpad-server.googlecode.com/files/a01.png](http://xpad-server.googlecode.com/files/a01.png)
  * Under _XBox 360 Controller settings_ enter the server IP address of your PC running the XPad Server. **Make sure WIFI is enabled!**
![http://xpad-server.googlecode.com/files/a03.png](http://xpad-server.googlecode.com/files/a03.png)
  * Run the XBox 360 Controller activity. Press Select IME and choose XBox 360 Controller
![http://xpad-server.googlecode.com/files/a02.png](http://xpad-server.googlecode.com/files/a02.png)
  * Press any button in the XBox controller. The event log will display the key code pressed.

### XBox 360 Android IME with other Apps ###
Unfortunately, Android IMEs require that an app have a text view to be able to receive input events from the IME. Thus if you wish to use our IME with other games or apps. The game/app in question **MUST HAVE A TEXT VIEW** in the current activity (perhaps somewhere under options). In the other hand, our wiki section shows how easy is to write a TCP client to connect to the XPad server.