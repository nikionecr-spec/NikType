#!/usr/bin/env bash
# امضای محلی APK ریلز NikType
# کلید امضا هرگز وارد مخزن نمی‌شود — این اسکریپت فقط محلی اجرا می‌شود.
#
# استفاده:
#   1) keytool -genkeypair -v -keystore niktype-release.jks -alias niktype \
#        -keyalg RSA -keysize 4096 -validity 10000
#   2) ./scripts/sign-apk.sh app-release-unsigned.apk niktype-release.jks niktype
set -euo pipefail

APK="${1:?Usage: sign-apk.sh <apk> <keystore> <alias>}"
KS="${2:?missing keystore}"
ALIAS="${3:?missing alias}"

if [ ! -f "$KS" ]; then
  echo "کلید $KS وجود ندارد. با keytool بسازید (README را ببینید)." >&2
  exit 1
fi

OUT="${APK%.apk}-signed.apk"
apksigner sign --ks "$KS" --ks-key-alias "$ALIAS" --out "$OUT" "$APK"
apksigner verify "$OUT"
echo "امضا شد: $OUT"
