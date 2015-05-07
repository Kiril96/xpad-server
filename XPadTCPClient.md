# Writing an XPad Server TCP Client #

Writing a TCP is really simple. Just create a socket to the host (default port is 5555), then receive and consume the network packets as encoded in the previous wiki.

Here is a simple client written in Java:

```
public class XPadClient {
	/* Size of the buffer sent by XPad server
	 * BYTE (1 byte - controller #)
	 * WORD wButtons; (2 bytes)
	 * BYTE bLeftTrigger; (1)
	 * BYTE bRightTrigger; (1)
	 * SHORT sThumbLX; (2)
	 * SHORT sThumbLY; (2)
	 * SHORT sThumbRX; (2)
	 * SHORT sThumbRY (2)
	 */
	public static final int XINPUT_BUFFER_SIZE = 13;

	private Socket mSocket;
	private BufferedInputStream bis;
	private BufferedOutputStream bos;
	
	private int mPort = 5555;
	private String mServer;
	private boolean mDone = false;
	
        private void connect() {
		if ( mServer == null || mServer.length() < 1) {
			sendError("A Server is required.");
			return;
		}
		Log.d(TAG, "Connecting to " + mServer + ":" + mPort);
	
		try {
			mSocket = new Socket(mServer, mPort);
			
			Log.d(TAG, "Connected.");
			
			sendConnected();
			
			// connected
			mDone = false;
			bis = new BufferedInputStream(mSocket.getInputStream());
			bos = new BufferedOutputStream(mSocket.getOutputStream());

			// msgs are 13 bytes
			byte[] buffer = new byte[XPadClient.XINPUT_BUFFER_SIZE];
			int read;
			XINPUT_GAMEPAD pad;
			
			while (! mDone) {
				read = bis.read(buffer);
				
				if ( read != -1) {
					//dumpBytes("BUF", buffer, read);
					
					// Unpack and consume
					pad = unpackBytes(buffer);
					parsePacket(pad);
				}
			}
			
			Log.d(TAG, "Connect thread done");
			
		} catch (Exception e) {
			sendError("Connection Error: " + e.getMessage());
		}
        }
	// ...
}

```

Given a Java class XINPU\_GAMEPAD to mirror its C++ counterpart you can unpack the bytes as follows:

```
	/**
	 * C++ - 
	 *  typedef struct _XINPUT_GAMEPAD {
	 *      WORD wButtons;
	 *      BYTE bLeftTrigger;
	 *      BYTE bRightTrigger;
	 *      SHORT sThumbLX;
	 *      SHORT sThumbLY;
	 *      SHORT sThumbRX;
	 *      SHORT sThumbRY;
	 *  } XINPUT_GAMEPAD, *PXINPUT_GAMEPAD;
	 */
	static class XINPUT_GAMEPAD {
		public byte bController;
		public short wButtons;
		public byte bLeftTrigger;
		public byte bRightTrigger;
		public short sThumbLX;
		public short sThumbLY;
		public short sThumbRX;
		public short sThumbRY;
	}
	
	short UNPACK_SHORT(byte hi, byte lo) {
		short n = (short) ((short)((hi & 0xFF) << 8) | (lo & 0xFF));
		return n;
	}

	/**
	 * Unpack a byte buffer sent by the server. The buffer size is
	 * XINPUT_BUFFER_SIZE (13) as follows:
	 * BYTE (1 byte - controller #)
	 * WORD wButtons; (2 bytes)
	 * BYTE bLeftTrigger; (1)
	 * BYTE bRightTrigger; (1)
	 * SHORT sThumbLX; (2)
	 * SHORT sThumbLY; (2)
	 * SHORT sThumbRX; (2)
	 * SHORT sThumbRY (2)
	 * @param buffer
	 * @return
	 */
	private XINPUT_GAMEPAD unpackBytes(byte[] buffer) {
		// check bufferssize here ... make sure it is 13 bytes
		XINPUT_GAMEPAD pad = new XINPUT_GAMEPAD();
		pad.bController = buffer[0];
		pad.wButtons = UNPACK_SHORT(buffer[1], buffer[2]); 
		pad.bLeftTrigger = buffer[3];
		pad.bRightTrigger = buffer[4];
		
		// Thumbsticks
		pad.sThumbLX = UNPACK_SHORT(buffer[5], buffer[6]);
		pad.sThumbLY = UNPACK_SHORT(buffer[7], buffer[8]);
		pad.sThumbRX = UNPACK_SHORT(buffer[9], buffer[10]);
		pad.sThumbRY = UNPACK_SHORT(buffer[11], buffer[12]);
		
		return pad;
	}
	
```

With XPad Server you can use your XBox 360 controller in any phone or tablet. Check out our Android IME!