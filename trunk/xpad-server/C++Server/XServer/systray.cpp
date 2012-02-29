#include "stdafx.h"
#include "XServer.h"

extern TCHAR szTitle[MAX_LOADSTRING];

NOTIFYICONDATA g_notifyIconData;
HMENU g_Menu;

void SysTrayInit ()
{
	memset(&g_notifyIconData, 0 , sizeof(NOTIFYICONDATA));

	g_notifyIconData.cbSize = sizeof(NOTIFYICONDATA); 
	g_notifyIconData.hWnd = g_hWnd;
	g_notifyIconData.uID = ID_TRAY_APP_ICON;
	g_notifyIconData.uFlags =  NIF_ICON | NIF_MESSAGE | NIF_TIP | NIF_INFO; 
	g_notifyIconData.uCallbackMessage = WM_TRAYICON;
	//g_notifyIconData.hIcon = (HICON)LoadImage(NULL, TEXT("XPadServer.ico"), IMAGE_ICON, 0, 0, LR_LOADFROMFILE);
	g_notifyIconData.hIcon = LoadIcon(hInst, MAKEINTRESOURCE(IDI_XSERVER));
	stringcopy(g_notifyIconData.szTip, TEXT("XPadServer"));
}

void SysTrayBallon(DWORD flags, const wchar_t * title, const wchar_t * msg)
{
	g_notifyIconData.cbSize = NOTIFYICONDATA_V2_SIZE; 
	g_notifyIconData.uFlags = NIF_INFO;
	g_notifyIconData.dwInfoFlags = flags;
	g_notifyIconData.uTimeout = 5000;

	stringcopy(g_notifyIconData.szInfo, msg);
	stringcopy(g_notifyIconData.szInfoTitle, title);

	Shell_NotifyIcon(NIM_MODIFY, &g_notifyIconData);
}


// const wchar_t * title, 
void SysTrayInfoBallon(const wchar_t * msg)
{
	SysTrayBallon(NIIF_INFO, szTitle, msg);
}


// const wchar_t * title, 
void SysTrayErrorBallon(const wchar_t * msg)
{
	SysTrayBallon(NIIF_ERROR, szTitle, msg);
}

void SysTrayInfoBallon_VA(char *format, ...)
{
	va_list         argptr;
	char     string[1024];

	va_start (argptr, format);
	vsprintf_s (string, format,argptr);
	va_end (argptr);
	
	size_t newsize = strlen(string) + 1;
	wchar_t * wcstring = new wchar_t[newsize];

	mbstowcs(wcstring, string, newsize);

	SysTrayInfoBallon(wcstring);

	delete [] wcstring;
}

void SysTrayErrorBallon_VA(char *format, ...)
{
	va_list  argptr;
	char     string[1024];

	va_start (argptr, format);
	vsprintf_s (string, format,argptr);
	va_end (argptr);
	
	size_t newsize = strlen(string) + 1;
	wchar_t * wcstring = new wchar_t[newsize];

	mbstowcs(wcstring, string, newsize);

	SysTrayErrorBallon( wcstring);

	delete [] wcstring;
}

void SysTrayInitMenu()
{
	g_Menu = CreatePopupMenu();
	AppendMenu(g_Menu, MF_STRING, ID_TRAY_XBOX1_CONTEXT_MENU_ITEM, TEXT("Connect USB XBOX Controller"));
	AppendMenu(g_Menu, MF_STRING, 0, NULL);
	AppendMenu(g_Menu, MF_STRING, ID_TRAY_EXIT_CONTEXT_MENU_ITEM, TEXT("Exit"));
}

void SysTrayAdd()
{
	// Show sys tray
	Shell_NotifyIcon(NIM_ADD, &g_notifyIconData);

	// Hide Win
	//ShowWindow(g_hWnd, SW_HIDE);
}

void SysTrayDispose()
{
	// remove sys tray
	//if ( ! IsWindowVisible(g_hWnd)) {
		Shell_NotifyIcon(NIM_DELETE, &g_notifyIconData);
	//}
}

void InitConsole()
{
	if ( !HAVE_CONSOLE ) return;

	AllocConsole();
	AttachConsole(GetCurrentProcessId());
	freopen("CON", "w", stdout); 
}

/**
 * Printf into the java layer
 * does a varargs printf into a temp buffer
 * and calls jni_sebd_str
 */
void LOGP(const char * prefix, char *format, ...)
{
	if ( !HAVE_CONSOLE ) return;

	va_list         argptr;
	static char     string[1024];

	va_start (argptr, format);
	vsprintf_s (string, format,argptr);
	va_end (argptr);

	printf("%s: %s", prefix, string);
}
