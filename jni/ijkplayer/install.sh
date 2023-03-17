#!/usr/bin/env bash

adb push libs/arm64-v8a/libijkffmpeg.so       /system/priv-app/MiuiCamera/lib/arm64/
adb push libs/arm64-v8a/libijkplayer.so       /system/priv-app/MiuiCamera/lib/arm64/
adb push libs/arm64-v8a/libijksdl.so          /system/priv-app/MiuiCamera/lib/arm64/

adb push libs/arm64-v8a/libijkffmpeg.so       /system/lib64/
adb push libs/arm64-v8a/libijkplayer.so       /system/lib64/
adb push libs/arm64-v8a/libijksdl.so          /system/lib64/

printf "Done!\\n"
