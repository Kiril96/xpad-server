#ifndef _XBOX_CONTROLLER_H
#define _XBOX_CONTROLLER_H

#define WIN32_LEAN_AND_MEAN

#include <windows.h>
#include <XInput.h>
#include "TCPServer.h"

// Link against XInput.lib
#pragma comment(lib, "XInput.lib")

#define MAX_CONTROLLERS 4
/*
typedef struct {
	int number;
	XINPUT_STATE state;
} CONTROLLER;
*/

class CXBOXController
{
private:
	// states of all controllers. Used to scan for changes
	//DWORD mStatuses[MAX_CONTROLLERS];
	XINPUT_GAMEPAD mStatuses[MAX_CONTROLLERS];

	//XINPUT_STATE _xBoxState;
	//int _xBoxNum;
	bool mDone;

	//CONTROLLER mControllers[MAX_CONTROLLERS];

	// Connect thread stuff
	HANDLE mTid;

	// TCP server to send key info
	CTCPServer * mServer;

	bool GamePadChanged(int i, XINPUT_GAMEPAD pad);

	void StoreGamePad(int i, XINPUT_GAMEPAD pad);

	// thread callback
	static int CXBOXController::cbProcess( void * pThis);

	// Worker method
	int CXBOXController::Process();

	void PackGamePad(BYTE num, XINPUT_GAMEPAD pad, BYTE * data);

	// GetStates of all controllers
	//void GetStates();
public:
	CXBOXController (); //int playerNum);
	//XINPUT_STATE GetState();

	bool IsConnected(int num);
	void Vibrate(int num, int leftVal = 0, int rightVal = 0 );

	// set the tcp server used to send bytes to
	void SetServer(CTCPServer * server);

	// Start worker thread
	void Connect();
	void Disconnect();
};

#endif
