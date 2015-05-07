# XInput Standard #
XInput is defined in C++ as
```
typedef struct _XINPUT_GAMEPAD {
    WORD wButtons;
    BYTE bLeftTrigger;
    BYTE bRightTrigger;
    SHORT sThumbLX;
    SHORT sThumbLY;
    SHORT sThumbRX;
    SHORT sThumbRY;
}
```

The XPad Server packs the above structure above as a 13 byte packet where:

```
 BYTE (1 - controller #)
 WORD wButtons; (2 bytes)
 BYTE bLeftTrigger; (1)
 BYTE bRightTrigger; (1)
 SHORT sThumbLX; (2)
 SHORT sThumbLY; (2)
 SHORT sThumbRX; (2)
 SHORT sThumbRY (2)
```

TCP Clients receive a 13 byte packet over the network

```
1     2    3    4    5    6   7   8   9   10    11   12   13
ctl# [wBtns]   LT   RT   [ LX  ] [ LY  ]  [  RX  ]  [  RY   ]
```

Encoded most significant byte first.

# References #
XInput Standard
  * http://msdn.microsoft.com/en-us/library/windows/desktop/ee417001%28v=vs.85%29.aspx

  * http://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinput_gamepad%28v=vs.85%29.aspx