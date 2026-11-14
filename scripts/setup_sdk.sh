#!/bin/bash
# نصب Android SDK محلی برای بیلد و تست
set -e
mkdir -p /home/z/android-sdk/cmdline-tools
cd /tmp/dl
if [ ! -d /home/z/tools/gradle-8.6 ]; then
  unzip -q -o gradle-8.6-bin.zip -d /home/z/tools
fi
if [ ! -d /home/z/android-sdk/cmdline-tools/latest ]; then
  rm -rf /tmp/sdk-tmp && mkdir -p /tmp/sdk-tmp
  unzip -q -o sdk-tools.zip -d /tmp/sdk-tmp
  mv /tmp/sdk-tmp/cmdline-tools /home/z/android-sdk/cmdline-tools/latest
fi
export ANDROID_HOME=/home/z/android-sdk
SDKM=/home/z/android-sdk/cmdline-tools/latest/bin/sdkmanager
yes | $SDKM --licenses > /tmp/lic.log 2>&1 || true
$SDKM "platform-tools" "platforms;android-34" "build-tools;34.0.0" > /tmp/sdk-install.log 2>&1
echo "SDK_SETUP_DONE"
