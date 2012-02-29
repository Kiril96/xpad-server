#include "stdafx.h"
#include "CXBOXController.h"
#include "util.h"
#include "systray.h"
#include <math.h>

#define THUMB_CHANGE_THRESHOLD 2000

/*
 * PAK helpers
 */
void PACK_SHORT(SHORT n, BYTE * ba, int offset) {
	ba[offset] = (BYTE)(n >> 8);
	ba[offset + 1] = (BYTE)(n & 0xFF);
}

void PACK_DUMP(BYTE * ba, size_t len) 
{
	for (int i = 0 ; i < len ; i++ ) printf("0x%02X ", ba[i]);
	printf("\n");
}

void DUMP_STATE(int n) 
{
	XINPUT_STATE state;
	ZeroMemory(&state, sizeof(XINPUT_STATE));
	DWORD dwResult = XInputGetState(n, &state);

	printf("%d: Result=%d Packet: %ld Btns: 0x%2X\n", n, dwResult, state.dwPacketNumber, state.Gamepad.wButtons);
}

CXBOXController::CXBOXController() 
{
	mDone = false;
	mTid = 0;
	mServer = NULL;

	// init statuses
	for ( int i = 0 ; i < MAX_CONTROLLERS ; i++ ) {
		ZeroMemory(&mStatuses[i], sizeof(XINPUT_GAMEPAD));
	}
}

void CXBOXController::SetServer(CTCPServer * server)
{
	mServer = server;
}


// Check the state of controller 0
bool CXBOXController::IsConnected(int num)
{
	XINPUT_STATE state;
	ZeroMemory(&state, sizeof(XINPUT_STATE));
	DWORD Result = XInputGetState(num, &state);

	LOGD("X360 ISConnected. Query 4 controller %d Result=%ld\n", num, Result);

	return ( Result == ERROR_SUCCESS ) ? true : false;
}

void CXBOXController::Vibrate(int num, int leftVal , int rightVal )
{
	XINPUT_VIBRATION Vibration;
	ZeroMemory(&Vibration, sizeof(XINPUT_VIBRATION));

	Vibration.wLeftMotorSpeed = leftVal;
	Vibration.wRightMotorSpeed = rightVal;

	XInputSetState(num, &Vibration); 
}

void CXBOXController::Connect()
{
	if ( mTid != 0 ) {
		SysTrayInfoBallon(TEXT("XBOX controller already connected."));
		return;
	}
	mTid = CreateThread (NULL, 0, (unsigned long (__stdcall *)(void *))this->cbProcess
		, (void *)this, 0, NULL);

	LOGD("Created X360 thread with handle %d\n", mTid);
}

void CXBOXController::Disconnect()
{
	mDone = true;
	mTid = 0;
}

// Thread callback
int CXBOXController::cbProcess( void * pThis)
{
	return ((CXBOXController *)(pThis))->Process();
}


void CXBOXController::PackGamePad(BYTE num, XINPUT_GAMEPAD pad, BYTE * data)
{
	data[0] = num;
	PACK_SHORT(pad.wButtons, data, 1);
	data[3] = pad.bLeftTrigger;
	data[4] = pad.bRightTrigger;
	PACK_SHORT(pad.sThumbLX, data, 5);
	PACK_SHORT(pad.sThumbLY, data, 7);
	PACK_SHORT(pad.sThumbRX, data, 9);
	PACK_SHORT(pad.sThumbRY, data, 11);
}

void DUMP_GAMEPAD(const char * lbl, XINPUT_GAMEPAD pad )
{
	printf("%s: BTNS:0x%X LT:0x%X RT:0x%X LX:%d LY:%d RX:%d RY:%d\n", lbl
	,pad.wButtons
	,pad.bLeftTrigger
	,pad.bRightTrigger
	,pad.sThumbLX
	,pad.sThumbLY
	,pad.sThumbRX
	,pad.sThumbRY	);
}

bool CXBOXController::GamePadChanged(int i, XINPUT_GAMEPAD pad)
{
	return (mStatuses[i].wButtons != pad.wButtons) 
		|| (mStatuses[i].bLeftTrigger != pad.bLeftTrigger)
		|| (mStatuses[i].bRightTrigger != pad.bRightTrigger)
		// use a threshold to detect changes
		|| (abs(mStatuses[i].sThumbLX - pad.sThumbLX) > THUMB_CHANGE_THRESHOLD )
		|| (abs(mStatuses[i].sThumbLY - pad.sThumbLY) > THUMB_CHANGE_THRESHOLD )	
		|| (abs(mStatuses[i].sThumbRX - pad.sThumbRX) > THUMB_CHANGE_THRESHOLD )
		|| (abs(mStatuses[i].sThumbRY - pad.sThumbRY) > THUMB_CHANGE_THRESHOLD )
		;
}

void CXBOXController::StoreGamePad(int i, XINPUT_GAMEPAD pad)
{
	mStatuses[i].wButtons = pad.wButtons;
	mStatuses[i].bLeftTrigger = pad.bLeftTrigger;
	mStatuses[i].bRightTrigger = pad.bRightTrigger;
	mStatuses[i].sThumbLX = pad.sThumbLX;
	mStatuses[i].sThumbLY = pad.sThumbLY;
	mStatuses[i].sThumbRX = pad.sThumbRX;
	mStatuses[i].sThumbRY = pad.sThumbRY;
}


// Worker method
int CXBOXController::Process()
{
	LOGD("Thread Loop ENTER\n");
	DWORD dwResult;
	XINPUT_STATE state;

	// controller data
	BYTE data[13];
	ZeroMemory(data, sizeof(data));

	while (! mDone) {
		// Get X360 states for all controllers
		for ( int i = 0 ; i < MAX_CONTROLLERS ; i++ ) {
			//XINPUT_STATE state;
			ZeroMemory(&state, sizeof(XINPUT_STATE));

			dwResult = XInputGetState(i, &state);

			if ( dwResult == ERROR_SUCCESS ) {
				// Process state
				//if ( state.dwPacketNumber != mStatuses[i] ) {
				if ( GamePadChanged(i, state.Gamepad)) {
					DUMP_GAMEPAD("GP", state.Gamepad);
					//DUMP_GAMEPAD("Old", mStatuses[i]);

					//mStatuses[i] = state.dwPacketNumber;
					StoreGamePad(i, state.Gamepad); 

					//LOGD("X360 Pad %d Paket N: %ld Status btns: %d\n", i, state.dwPacketNumber, state.Gamepad.wButtons);

					// Status changed. Send it
					PackGamePad((BYTE)i, state.Gamepad, data);

					PACK_DUMP(data, sizeof(data));
					
					// Send bytes to client
					if ( mServer ) {
						mServer->SendBytes(data, sizeof(data));
					}
					else {
						LOGE("No Server available!\n");
					}
				}
			}
		} 
	} 
	LOGD("Thread Loop COMPLETE\n");
	return 0;
}
