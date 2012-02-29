#include "StdAfx.h"

#include "TCPServer.h"
#include "util.h"
#include "systray.h"

// Utility
void DUMP_IPS() 
{
	char ac[80];
	if ( gethostname(ac, sizeof(ac)) == SOCKET_ERROR ) {
		return;
	}

	LOGD("Host name: %s\n", ac);

	struct hostent *phe = gethostbyname(ac);
	if ( ! phe ) return;

	for ( int i = 0 ;  phe->h_addr_list[i] != 0 ; i++ ) {
		struct in_addr addr;
		memcpy(&addr, phe->h_addr_list[i], sizeof(struct in_addr));

		LOGD("IP Address [%d] = %s\n", i , inet_ntoa(addr));
	}
}

/*
 * Constructor
 */
CTCPServer::CTCPServer(int port)
{
	mPort = port; //5555;
	mDone = false;
	mSockClient = INVALID_SOCKET; // NULL; 
	mTid = 0;
	LOGD("Server @ port %d\n", mPort);
}


CTCPServer::~CTCPServer(void)
{
}


void CTCPServer::Start(void)
{
	mTid = CreateThread (NULL, 0, (unsigned long (__stdcall *)(void *))this->cbProcess
		, (void *)this, 0, NULL);

	LOGD("Started TCP Server thread with handle %d\n", mTid);
}

// Thread callback
int CTCPServer::cbProcess( void * pThis)
{
	return ((CTCPServer *)(pThis))->Process();
}

// Thread Worker method
int CTCPServer::Process()
{
	LOGD("TCP Server Process\n");
	SOCKET server;

	WSADATA wsaData;
	sockaddr_in local;

	// init winsock
	if ( WSAStartup(MAKEWORD(2,2), &wsaData)) {
		SysTrayErrorBallon(TEXT("Winsock init error"));
		return -1;
	}

	DUMP_IPS();

	local.sin_family = AF_INET;
	local.sin_addr.s_addr = INADDR_ANY;
	local.sin_port = htons((u_short)mPort);

	// create server socket
	server = socket (AF_INET, SOCK_STREAM, 0);
	
	if ( server == INVALID_SOCKET ) {
		SysTrayErrorBallon_VA("Invalid Server socket @ port %d", mPort);
		return -1;
	}

	if ( bind(server, (sockaddr *)&local, sizeof(local)) != 0 ) {
		SysTrayErrorBallon_VA("Server: Unable to bind to port %d", mPort);
		return -1;
	}

	if ( listen(server, 10) != 0 ) {
		SysTrayErrorBallon_VA("Server: Listen error %ld", WSAGetLastError());
		return -1;
	}

	//SysTrayInfoBallon_VA("Accepting clients @ IP address %s port %d.", inet_ntoa(local.sin_addr), mPort );
	SysTrayInfoBallon_VA("Accepting clients @ port %d. Right click for options.", mPort );

	// client
	sockaddr_in from;
	int fromlen = sizeof(from);

	while (! mDone) {
		if ( mSockClient == INVALID_SOCKET ) {
			LOGD("Accepting clients...\n");

			mSockClient = accept(server , (struct sockaddr *)&from, &fromlen);

			//SysTrayInfoBallon(TEXT("Got a TCP client connection."));
			SysTrayInfoBallon_VA("Got a TCP client connection from %s", inet_ntoa(from.sin_addr) );
		}
		else {
			//LOGD("Client connected. Sleep...\n");
			Sleep(1000);
		}
	} 
	return 0;
}


void CTCPServer::Stop(void)
{
	closesocket(mSockClient);

	// stop thread
	mDone = true;
	mSockClient = INVALID_SOCKET; // NULL; 
	WSACleanup();
}

void CTCPServer::DisconnectClient(void)
{
	closesocket(mSockClient);
	mSockClient = INVALID_SOCKET; // NULL;
}

void CTCPServer::SendBytes(BYTE * data, size_t len)
{
	if ( mSockClient == INVALID_SOCKET) {
		LOGE("Invalid client socket\n");
		return;
	}

	// Send bytes
	int iRet =  send(mSockClient, (char *)data, len, 0);

	if ( iRet == SOCKET_ERROR ) {
		LOGE("Socket Send failure\n");
		DisconnectClient();

		SysTrayErrorBallon(TEXT("Send failure. Client disconnected."));
	}
}
