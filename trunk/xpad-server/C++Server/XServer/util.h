#pragma once

#define LOGD(...) LOGP("DEGUG", __VA_ARGS__)
#define LOGE(...) LOGP("ERROR", __VA_ARGS__)

// util
void InitConsole();
void LOGP(const char * prefix, char *format, ...);
