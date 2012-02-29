#pragma once

#include "resource.h"

#define WM_TRAYICON (WM_USER + 1)

#define ID_TRAY_APP_ICON 5000

// Sys tray menu opts 

// Quit
#define ID_TRAY_EXIT_CONTEXT_MENU_ITEM 3000

// Connect Xbox
#define ID_TRAY_XBOX1_CONTEXT_MENU_ITEM 3001

extern NOTIFYICONDATA g_notifyIconData;
extern HWND g_hWnd;
extern HINSTANCE hInst;
extern HMENU g_Menu;

#ifdef UNICODE
#define stringcopy wcscpy
#else
#define stringcopy strcpy
#endif

#define MAX_LOADSTRING 100
