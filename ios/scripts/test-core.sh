#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
# Command Line Tools ships Testing.framework outside SwiftPM's default lookup path.
if [ "$(xcode-select -p)" = /Library/Developer/CommandLineTools ]; then
  export ZOT_CORE_ONLY=1
  exec swift test --disable-xctest --package-path ZotMacrosCore -Xswiftc -F -Xswiftc /Library/Developer/CommandLineTools/Library/Developer/Frameworks -Xlinker -rpath -Xlinker /Library/Developer/CommandLineTools/Library/Developer/Frameworks -Xlinker -rpath -Xlinker /Library/Developer/CommandLineTools/Library/Developer/usr/lib "$@"
fi
exec swift test --package-path ZotMacrosCore "$@"
