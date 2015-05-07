#Setting up XPad Server with the PS3 Controller.

# Setting up XPad Server with the PS3 Controller #

Follow these instructions to setup your PS3 controller.

# Instructions #

  * Download the MotioninJoy PS3 manager from http://www.motioninjoy.com/download
  * Install MotioninJoy and run the DS3Tool (see figure 1).
  * Connect your PS3 controller via USB to your PC. Click the **Driver Manager** tab in the DS3Tool and click the **Load Driver** button. This will install the SixAxis driver packed with MotioninJoy in your PC(see figure1).

![http://xpad-server.googlecode.com/files/joy1.png](http://xpad-server.googlecode.com/files/joy1.png)

  * After the driver installation completes you can test your PS3 controller! Click the **Profiles** in the DS3Tool (see figure 2).
  * You will see the connected game controller. Click the **Vibration testing** button at the bottom to test it!
  * Select **XInput-Default** in the controller mode. **This is important**. It will translate PS3 controller events into XINPUT events understood by the Xpad server.

![http://xpad-server.googlecode.com/files/joy2.png](http://xpad-server.googlecode.com/files/joy2.png)

  * You can now run the XPad Server. Right click **Connect USB XBOX Controller** from the system tray and you are ready to go!

![http://xpad-server.googlecode.com/files/st2.jpg](http://xpad-server.googlecode.com/files/st2.jpg)

## PS3 in Wireless Mode ##
To use your controller in wireless mode you need a bluetooth dongle supported by MotioninJoy. _Note: Your default PC/Laptop Bluetooth adapter will not work!_

  * For a list of compatible BT dongles see http://www.motioninjoy.com/wiki/en/help/btcompatibility
  * Connect your PS3 Controller to your PC via USB.
  * Connect your BT dongle to your PC/Laptop (via USB) and run the D3Tool

![http://xpad-server.googlecode.com/files/joy3.png](http://xpad-server.googlecode.com/files/joy3.png)

  * Click the **BlueTooth Pair** tab at the top (see figure 3). Your BT dongle should display in the adapter section. _If it does not it means your BT dongle is NOT compatible with MotioninJoy!_ Your PS3 Controller will display in the **DUALSHOCK** section (If it does not hit the **Reload this page** button in the right corner).
  * Click the **Pair Now** button at the bottom. Unplug your PS3 USB cable. Your PS3 is ready for wireless now. Press the PS button in the controller to connect to the PC via BT and press the **Vibration Testing** in the **Profiles** section of the DS3Tool to test it. You are ready to rumble!