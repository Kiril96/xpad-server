#pragma once

#include "StdAfx.h"

#include <WinSock2.h>
#include <WS2tcpip.h>

// Link against ws2_32.lib
#pragma comment(lib, "ws2_32.lib")

class CTCPServer
{
private:
	int mPort;
	bool mDone;

	// client socket
	SOCKET mSockClient;

	// Connect thread stuff
	HANDLE mTid;

	// thread callback
	static int CTCPServer::cbProcess( void * pThis);

	// Worker method
	int CTCPServer::Process();

public:
	CTCPServer(int port);
	~CTCPServer(void);

	// Start/Stop the server thread
	void Start(void);
	void Stop(void);
	
	// Disconnect client
	void DisconnectClient();
	void SendBytes(BYTE * data, size_t len);
};

