#pragma once

// Systray
void SysTrayInit();
void SysTrayDispose();
void SysTrayAdd();
void SysTrayInitMenu();
/*
void SysTrayInfoBallon(const wchar_t * title, const wchar_t * msg);
void SysTrayErrorBallon(const wchar_t * title, const wchar_t * msg);
*/
void SysTrayInfoBallon(const wchar_t * msg);
void SysTrayErrorBallon(const wchar_t * msg);

void SysTrayInfoBallon_VA(char *format, ...);
void SysTrayErrorBallon_VA(char *format, ...);
