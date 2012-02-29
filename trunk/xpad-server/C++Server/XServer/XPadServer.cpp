// XServer.cpp : Defines the entry point for the application.
//

#include "stdafx.h"
#include "XServer.h"
#include "util.h"
#include "systray.h"

#include "TCPServer.h"

// controller(s)
#include "CXBOXController.h"

// Global Variables:
HINSTANCE hInst;								// current instance
HWND g_hWnd;									// current window

// Debug console
bool HAVE_CONSOLE = false;

// def server PORT
int DEFAULT_SERVER_PORT = 5555;

UINT WM_TASKBARCREATED = 0;

TCHAR szTitle[MAX_LOADSTRING];					// The title bar text
TCHAR szWindowClass[MAX_LOADSTRING];			// the main window class name

// Controllers
CXBOXController * xBoxController;

// Server: Used to send controller bytes
CTCPServer * mTcpServer;

// Forward declarations of functions included in this code module:
ATOM				MyRegisterClass(HINSTANCE hInstance);
BOOL				InitInstance(HINSTANCE, int);
LRESULT CALLBACK	WndProc(HWND, UINT, WPARAM, LPARAM);
INT_PTR CALLBACK	About(HWND, UINT, WPARAM, LPARAM);

// Initialize controllers
void InitControllers();

int APIENTRY _tWinMain(HINSTANCE hInstance,
                     HINSTANCE hPrevInstance,
                     LPTSTR    lpCmdLine,
                     int       nCmdShow)
{
	UNREFERENCED_PARAMETER(hPrevInstance);
	UNREFERENCED_PARAMETER(lpCmdLine);

 	// Place code here.
	MSG msg;
	HACCEL hAccelTable;

	// Initialize global strings
	LoadString(hInstance, IDS_APP_TITLE, szTitle, MAX_LOADSTRING);
	LoadString(hInstance, IDC_XSERVER, szWindowClass, MAX_LOADSTRING);
	MyRegisterClass(hInstance);

	// Perform application initialization:
	if (!InitInstance (hInstance, nCmdShow))
	{
		return FALSE;
	}
	
	
	SysTrayInfoBallon(TEXT("Welcome. Right click for options."));

	hAccelTable = LoadAccelerators(hInstance, MAKEINTRESOURCE(IDC_XSERVER));

	// Main message loop:
	while (GetMessage(&msg, NULL, 0, 0))
	{
		if (!TranslateAccelerator(msg.hwnd, hAccelTable, &msg))
		{
			TranslateMessage(&msg);
			DispatchMessage(&msg);
		}
	}

	SysTrayDispose();

	return (int) msg.wParam;
}



//
//  FUNCTION: MyRegisterClass()
//
//  PURPOSE: Registers the window class.
//
//  COMMENTS:
//
//    This function and its usage are only necessary if you want this code
//    to be compatible with Win32 systems prior to the 'RegisterClassEx'
//    function that was added to Windows 95. It is important to call this function
//    so that the application will get 'well formed' small icons associated
//    with it.
//
ATOM MyRegisterClass(HINSTANCE hInstance)
{
	WNDCLASSEX wcex;

	wcex.cbSize = sizeof(WNDCLASSEX);

	wcex.style			= CS_HREDRAW | CS_VREDRAW;
	wcex.lpfnWndProc	= WndProc;
	wcex.cbClsExtra		= 0;
	wcex.cbWndExtra		= 0;
	wcex.hInstance		= hInstance;
	wcex.hIcon			= LoadIcon(hInstance, MAKEINTRESOURCE(IDI_XSERVER));
	wcex.hCursor		= LoadCursor(NULL, IDC_ARROW);
	wcex.hbrBackground	= (HBRUSH)(COLOR_WINDOW+1);
	wcex.lpszMenuName	= MAKEINTRESOURCE(IDC_XSERVER);
	wcex.lpszClassName	= szWindowClass;
	wcex.hIconSm		= LoadIcon(wcex.hInstance, MAKEINTRESOURCE(IDI_SMALL));

	return RegisterClassEx(&wcex);
}

//
//   FUNCTION: InitInstance(HINSTANCE, int)
//
//   PURPOSE: Saves instance handle and creates main window
//
//   COMMENTS:
//
//        In this function, we save the instance handle in a global variable and
//        create and display the main program window.
//
BOOL InitInstance(HINSTANCE hInstance, int nCmdShow)
{
   //HWND hWnd;

   hInst = hInstance; // Store instance handle in our global variable

   g_hWnd = CreateWindow( szWindowClass, szTitle, WS_OVERLAPPEDWINDOW,
      CW_USEDEFAULT, 0, CW_USEDEFAULT, 0, NULL, NULL, hInstance, NULL);

   if (!g_hWnd)
   {
      return FALSE;
   }

   // Parse args
	for (int i = 0 ; i < __argc ; i++ ) {
		if ( wcscmp( __wargv[i], _T("-console")) == 0 ) {
			HAVE_CONSOLE = true;
		}
		if ( wcscmp( __wargv[i], _T("-port")) == 0 ) {
			DEFAULT_SERVER_PORT = _wtoi(__wargv[i + 1]);
		}
	}


   InitConsole();

   // Init sys tray
   SysTrayInit();
   SysTrayAdd();
   SysTrayInitMenu();

   // Init the TCP Server
   mTcpServer = new CTCPServer(DEFAULT_SERVER_PORT);

   // start the server thread
   mTcpServer->Start();

   // Init Controller(s)
   InitControllers();

   // Get notified when taskbar is relaunched
   WM_TASKBARCREATED = RegisterWindowMessageA("TaskbarCreated");

   //ShowWindow(g_hWnd, nCmdShow);
   UpdateWindow(g_hWnd);

   return TRUE;
}

//
//  FUNCTION: WndProc(HWND, UINT, WPARAM, LPARAM)
//
//  PURPOSE:  Processes messages for the main window.
//
//  WM_COMMAND	- process the application menu
//  WM_PAINT	- Paint the main window
//  WM_DESTROY	- post a quit message and return
//
//
LRESULT CALLBACK WndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
	int wmId, wmEvent;
	PAINTSTRUCT ps;
	HDC hdc;

	//printf("WndProc msg=%d/%d\n", message, WM_TASKBARCREATED);

	switch (message)
	{
	case WM_CREATE:
		LOGD("WM_CREATE\n"); 
		break;

	case WM_TRAYICON:
		//LOG("Tray icon notification from %d\n", wParam);
		if ( lParam == WM_RBUTTONDOWN) {
			LOGD("Tray icon Ctx menu\n");

			// get cur position
			POINT curPoint;
			GetCursorPos(&curPoint);

			UINT clicked = TrackPopupMenu(g_Menu, TPM_RETURNCMD | TPM_NONOTIFY
				, curPoint.x, curPoint.y, 0 , g_hWnd, NULL);

			// Connect to Xbox controller
			if ( clicked == ID_TRAY_XBOX1_CONTEXT_MENU_ITEM) {
				// 1st connected?
				if ( !xBoxController->IsConnected(0) ) {
					SysTrayErrorBallon(TEXT("Unable to find an X360 controller. Connect the controller to the USB port and try again."));
				}
				else {
					SysTrayInfoBallon(TEXT("XBOX 360 gamepad is alive!"));

					// Connect using threads
					xBoxController->Connect();
				} 
			}
			// Quit
			else if ( clicked == ID_TRAY_EXIT_CONTEXT_MENU_ITEM) {
				xBoxController->Disconnect();
				mTcpServer->Stop();	

				PostQuitMessage(0);
			}
		}
		break;

	case WM_COMMAND:
		wmId    = LOWORD(wParam);
		wmEvent = HIWORD(wParam);
		// Parse the menu selections:
		switch (wmId)
		{
		case IDM_ABOUT:
			DialogBox(hInst, MAKEINTRESOURCE(IDD_ABOUTBOX), hWnd, About);
			break;
		case IDM_EXIT:
			DestroyWindow(hWnd);
			break;
		default:
			return DefWindowProc(hWnd, message, wParam, lParam);
		}
		break;
	case WM_PAINT:
		hdc = BeginPaint(hWnd, &ps);
		// TODO: Add any drawing code here...
		EndPaint(hWnd, &ps);
		break;
	case WM_DESTROY:
		PostQuitMessage(0);
		break;
	default:
		return DefWindowProc(hWnd, message, wParam, lParam);
	}
	return 0;
}

// Message handler for about box.
INT_PTR CALLBACK About(HWND hDlg, UINT message, WPARAM wParam, LPARAM lParam)
{
	UNREFERENCED_PARAMETER(lParam);
	switch (message)
	{
	case WM_INITDIALOG:
		return (INT_PTR)TRUE;

	case WM_COMMAND:
		if (LOWORD(wParam) == IDOK || LOWORD(wParam) == IDCANCEL)
		{
			EndDialog(hDlg, LOWORD(wParam));
			return (INT_PTR)TRUE;
		}
		break;
	}
	return (INT_PTR)FALSE;
}

// Initialize controller objs
void InitControllers()
{
	LOGD("Init controllers\n");
	xBoxController = new CXBOXController(); //1);
	xBoxController->SetServer(mTcpServer);
}
